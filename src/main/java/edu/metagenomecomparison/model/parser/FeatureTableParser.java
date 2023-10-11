package edu.metagenomecomparison.model.parser;


import edu.metagenomecomparison.model.ComparativeTreeNode;
import edu.metagenomecomparison.model.TaxonRank;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.phylo.TreeParseException;
import jloda.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class FeatureTableParser extends TreeParser {

    public FeatureTableParser(){
        this.tree = new PhyloTree();
        this.numReadFiles = 0;
        this.readSamples = new ArrayList<>();
    }


    public PhyloTree read(String path){
        try (BufferedReader br
                     = new BufferedReader(new FileReader(path))) {
            ComparativeTreeNode root = new ComparativeTreeNode(tree);
            root.setLabel("Root");
            root.setRank(TaxonRank.ROOT);
            tree.setRoot(root);
            root.setParentTaxonNames(new String[]{"NCBI"});
            String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("# "))
                continue;
            if (line.startsWith("#OTU ID")){
                String[] datasetLine = line.split("\t");
                String [] sampleNames = Arrays.copyOfRange(datasetLine, 1, datasetLine.length);
                readSamples = new ArrayList<>(Arrays.asList(sampleNames));
                for (int i = 0; i < readSamples.size(); i++){
                    root.setAssigned(i, 0);
                    root.setSummed(i, 0);
                }
            }
            else {
                String taxonPath = StringUtils.getWordBetween("", "\t", line);
                String[] pathNodes = taxonPath.split(";");
                //TODO check for empty parts
                for (int j = 0; j < pathNodes.length; j++){
                    if (pathNodes[j].split("__").length <= 1 ) {
                        pathNodes = Arrays.copyOfRange(pathNodes, 0, j);
                        break;
                    }
                }
                String[] countsAssigned = StringUtils.getWordBetween("\t", "\n", line).split("\t");
                ComparativeTreeNode newNode = addNodesByPath(pathNodes);
                for (int i = 0; i < countsAssigned.length; i++) {
                    newNode.setAssigned(i, (int) Double.parseDouble(countsAssigned[i]));
                    newNode.setSummed(i, 0);
                }
            }

        }
        for (numReadFiles = 0; numReadFiles < readSamples.size(); numReadFiles++){
                tree.postorderTraversal(tree.getRoot(), giveCountToParent);
                tree.preorderTraversal(tree.getRoot(), computeTotalsAndSetNumSamples);
        }
            return this.tree;
        } catch (IOException | TreeParseException e) {
        throw new RuntimeException(e);
        }
    }


    public ComparativeTreeNode addNodesByPath(String[] pathNodes) throws TreeParseException {
        Node v = this.tree.getRoot();
        for (int i = 0; i < pathNodes.length; i++) {
            String nodeString;
            String rankChar;
            nodeString = pathNodes[i].split("__")[1];
            rankChar = pathNodes[i].split("__")[0];

            if (i == pathNodes.length - 1) {
                for (Node u : v.adjacentNodes()) {
                    if (Objects.equals(u.getLabel(), nodeString)) {
                        return (ComparativeTreeNode) u;
                    }
                }
            }
            String[] parentTaxonNames = pathNodes.clone();
            for (int j = 0; j < parentTaxonNames.length; j++){
                parentTaxonNames[j] = StringUtils.getTextAfter("__", pathNodes[j]);
            }
            boolean foundNode = false;
                for (Node u : v.adjacentNodes()) {
                    if (Objects.equals(u.getLabel(), nodeString)) {
                        v = u;
                        foundNode = true;
                        break;
                    }
                }
                if (!foundNode) {
                    ComparativeTreeNode inMedNode = new ComparativeTreeNode(tree);
                    inMedNode.setLabel(nodeString);
                    inMedNode.setRank(TaxonRank.taxonRankMap().get(rankChar));
                    inMedNode.setParentTaxonNames(Arrays.copyOfRange(parentTaxonNames, 0, i));
                    tree.newEdge(v, inMedNode);
                    v = inMedNode;
                    if (i == pathNodes.length - 1) {
                        return inMedNode;
                    }
                }

        }
        throw new TreeParseException("for some reason couldnt add or find node");
    }

    Consumer<Node> computeTotalsAndSetNumSamples = new Consumer<Node>() {
        @Override
        public void accept(Node node) {
            ComparativeTreeNode v = (ComparativeTreeNode) node;
            ComparativeTreeNode root =  ((ComparativeTreeNode) ((PhyloTree) v.getOwner()).getRoot());
            for (int i = 0; i <= numReadFiles; i++){
                v.setNumSamples(i, readSamples.get(i),
                        root.getSummed(i));
            }
            v.calculateTotalsFromScratch();
        }
    };

    Consumer<Node> giveCountToParent = new Consumer<Node>() {
        @Override
        public void accept(Node node) {
            ComparativeTreeNode v = (ComparativeTreeNode) node;
            ComparativeTreeNode u = (ComparativeTreeNode) v.getParent();
            if (u == null)
                return;
            int parentSummed = u.getSummedForTraversal(numReadFiles);
            int childAssigned = v.getAssigned(numReadFiles);
            int childSummed = v.getSummedForTraversal(numReadFiles);
            u.setSummed(numReadFiles, parentSummed + childSummed + childAssigned);
        }
    };


    @Override
    public Service<PhyloTree> readingService(File... file) {
        return new Service<PhyloTree>() {
            @Override
            protected Task<PhyloTree> createTask() {
                return new Task<PhyloTree>() {
                    @Override
                    protected PhyloTree call() throws Exception {
                        File single = file[0];
                        updateMessage("Reading feature table " + single.getName());
                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, 1);
                        read(single.getAbsolutePath());
                        updateMessage("");
                        updateProgress(0, 0);
                        return getTree();
                    }
                };
            }
        };    }

}
