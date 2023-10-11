package edu.metagenomecomparison.model.parser;

import edu.metagenomecomparison.model.ComparativeTreeNode;
import edu.metagenomecomparison.model.TaxonRank;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.phylo.TreeParseException;
import jloda.util.StringUtils;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

//TODO parse different taxonomies / metabolisms?

/**
 * this class is a parser for a file exported from megan with the options:
 * export text/csv format, taxonPath_to_count, assigned, tab
 * when reading multiple files to generate a shared tree, construct once and use read method once per file
 */
public class MeganSingleFileParser extends TreeParser {

    public MeganSingleFileParser() {
        this.tree = new PhyloTree();
        numReadFiles = 0;
        readSamples = new ArrayList<>();
    }

    /**
     * read one file from one sample exported from megan with the options:
     * export text/csv format, taxonPath_to_count, assigned, tab.
     * this method can be called multiple times to generate a shared tree.
     *
     * @param path the next path to read, use absolute path
     * @return the current tree
     */
    public PhyloTree readSingleFile(String path) {
        boolean isWithRanks;
        if (path.contains("/"))
            readSamples.add(path.substring(path.lastIndexOf("/")));
        else
            readSamples.add(path);
        try (BufferedReader br
                     = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line.startsWith("0__NCBI"))
                isWithRanks = true;
            else if (line.startsWith("\"NCBI"))
                isWithRanks = false;
            else
                throw new RuntimeException("Wrong File Format! Please open MEGAN exported tsv file with NCBI taxonomy");
            while ((line != null)) {
                    String[] pathNodes;
                    String taxonPath = isWithRanks ? StringUtils.getWordBetween("", "\t", line)
                            : StringUtils.getWordBetween("\"", "\"", line);
                    int countAssigned = (int) Double.parseDouble(StringUtils.getWordBetween("\t","\n",  line).strip());
                    int countSummed = 0;
                    pathNodes = taxonPath.split(";");
                    ComparativeTreeNode newNode;

                    if (taxonPath.equals("NCBI;") || taxonPath.equals("0__NCBI;")) {
                        if (numReadFiles == 0) {
                            newNode = new ComparativeTreeNode(tree);
                            tree.setRoot(newNode);
                            newNode.setRank(TaxonRank.ROOT);
                            newNode.setLabel("NCBI");
                            newNode.setParentTaxonNames(new String[]{"NCBI"});
                        } else
                            newNode = (ComparativeTreeNode) tree.getRoot();

                    } else {
                        newNode = addNodeByPath(pathNodes, tree, isWithRanks);
                    }
                    newNode.setAssigned(numReadFiles, countAssigned);
                    newNode.setSummed(numReadFiles, countSummed);
                    line = br.readLine();
            }
            tree.postorderTraversal(tree.getRoot(), giveCountToParent);
            tree.preorderTraversal(tree.getRoot(), computeTotalsAndSetNumSamples);
            this.numReadFiles++;
            return tree;
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
    //a
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




    //TODO check if this preserves ordering
    public void setTree(PhyloTree tree){
        this.tree = tree;
        this.numReadFiles = ((ComparativeTreeNode) tree.getRoot()).getNumSamples();
        this.readSamples = new ArrayList<>(Arrays.asList(((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().keySet().toArray(new String[0])));
    }


    /**
     * construct a service for reading one or more files representing one sample
     * @param filesToRead files to read
     * @return a phylotree
     */
    public Service<PhyloTree> readingService(File... filesToRead){
        return new Service<PhyloTree>() {
            @Override
            protected Task<PhyloTree> createTask() {
                return new Task<PhyloTree>() {
                    @Override
                    protected PhyloTree call() throws Exception {
                        int numFiles = filesToRead.length;
                        int i = 0;
                        for (File file: filesToRead){
                            updateProgress(i, numFiles);
                            updateMessage("Reading file " + file.getName());
                            readSingleFile(file.getAbsolutePath());
                            updateValue(getTree());
                            i++;
                        }
                        return getTree();
                    }
                };
            }
        };
    }




}
