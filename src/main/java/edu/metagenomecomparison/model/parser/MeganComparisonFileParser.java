package edu.metagenomecomparison.model.parser;

import edu.metagenomecomparison.model.ComparativeTreeNode;
import edu.metagenomecomparison.model.TaxonRank;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.phylo.TreeParseException;
import jloda.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class MeganComparisonFileParser extends TreeParser{

    /**
     * reads a MEGAN comparison file. exported from megan with the options:
     * export text/csv format, taxonPath_to_count, assigned, tab. this method should only be called once
     * on a new instance of MeganPathCountParser.
     * @param path path of the file to read
     */
    public PhyloTree readComparisonFile(String path){
        try (BufferedReader br
                     = new BufferedReader(new FileReader(path))) {
            String line;
            String[] sampleNames;
            boolean isWithRanks;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#Datasets")) {
                    String[] datasetLine = line.split("\t");
                    sampleNames = Arrays.copyOfRange(datasetLine, 1, datasetLine.length);
                    readSamples = new ArrayList<>(Arrays.asList(sampleNames));
                } else  {
                    isWithRanks = (line.startsWith("0__NCBI"));
                    String taxonPath = isWithRanks ? StringUtils.getWordBetween("", "\t", line)
                            : StringUtils.getWordBetween("\"", "\"", line);
                    String[] pathNodes = taxonPath.split(";");
                    String[] countsAssigned = StringUtils.getWordBetween("\t", "\n", line).split("\t");
                    ComparativeTreeNode newNode;
                    if (taxonPath.equals("NCBI;") || taxonPath.equals("0__NCBI;")) {
                        newNode = new ComparativeTreeNode(tree);
                        tree.setRoot(newNode);
                        newNode.setLabel("NCBI");
                        newNode.setParentTaxonNames(new String[]{"NCBI"});
                        newNode.setRank(TaxonRank.ROOT);
                    } else {
                        newNode = addNodeByPath(pathNodes, tree, isWithRanks);
                    }
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

    /**
     * adds a Node and returns it based on the path given by pathNodes
     * if Node is already present, just returns it
     *
     * @param pathNodes   an array of node names to traverse to get to the node to create/return
     * @param graph       the graph
     * @param isWithRanks true if the pathNodes have a prefix to indicate the rank
     * @return the node created or found
     */
    private ComparativeTreeNode addNodeByPath(String[] pathNodes, Graph graph, boolean isWithRanks) throws TreeParseException {
        Node v = graph.getFirstNode();
        for (int i = 1; i < pathNodes.length; i++) {
            String nodeString;
            String rankChar;
            if (isWithRanks) {
                nodeString = pathNodes[i].split("__")[1];
                rankChar = pathNodes[i].split("__")[0];
            }
            else {
                nodeString = pathNodes[i];
                rankChar = "0";
            }

            if (i == pathNodes.length - 1) {
                for (Node u : v.adjacentNodes()){
                    if (Objects.equals(u.getLabel(), nodeString)) {
                        return (ComparativeTreeNode) u;
                    }
                }
                ComparativeTreeNode newNode = new ComparativeTreeNode(graph);
                String[] parentTaxonNames = pathNodes.clone();
                if (isWithRanks){
                    for (int j = 0; j < parentTaxonNames.length; j++){
                        parentTaxonNames[j] = StringUtils.getTextAfter("__", pathNodes[j]);
                    }
                }

                newNode.setParentTaxonNames(parentTaxonNames);
                newNode.setRank(TaxonRank.taxonRankMap().get(rankChar));
                graph.newEdge(v, newNode);
                newNode.setLabel(nodeString);
                return newNode;
            }
            for (Node u : v.adjacentNodes()) {
                if (Objects.equals(u.getLabel(), nodeString)) {
                    v = u;
                    break;
                }
            }
        }
        throw new TreeParseException("for some reason couldnt add or find node");
    }




    @Override
    public Service<PhyloTree> readingService(File... file) {
        return new Service<PhyloTree>() {
            @Override
            protected Task<PhyloTree> createTask() {
                return new Task<PhyloTree>() {
                    @Override
                    protected PhyloTree call() throws Exception {
                        File comparisonFile = file[0];
                        updateMessage("Reading Comparison File " + comparisonFile.getName());
                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, 1);
                        readComparisonFile(comparisonFile.getAbsolutePath());
                        updateMessage("");
                        updateProgress(0, 0);
                        return getTree();
                    }
                };
            }
        };    }
}
