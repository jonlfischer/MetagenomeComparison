package edu.metagenomecomparison.model;

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
public class MeganPathCountParser {
    private int numReadFiles;
    private PhyloTree tree;


    private ArrayList<String> readSamples;



    public MeganPathCountParser() {
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

    //TODO check if this preserves ordering
    public void setTree(PhyloTree tree){
        this.tree = tree;
        this.numReadFiles = ((ComparativeTreeNode) tree.getRoot()).getNumSamples();
        this.readSamples = new ArrayList<>(Arrays.asList(((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().keySet().toArray(new String[0])));
    }

    /**
     * get the tree constructed from the parser. can be called after multiple calls to read for shared tree
     * @return the tree
     */
    public PhyloTree getTree() {
        return tree;
    }

    /**
     * construct a service for reading a megan comparison file
     * @param comparisonFile the megan comparison file
     * @return a phylotree
     */
    public Service<PhyloTree> meganReadingService(File comparisonFile){
        return new Service<PhyloTree>() {
            @Override
            protected Task<PhyloTree> createTask() {
                return new Task<PhyloTree>() {
                    @Override
                    protected PhyloTree call() throws Exception {
                        updateMessage("Reading Comparison File " + comparisonFile.getName());
                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, 1);
                        readComparisonFile(comparisonFile.getAbsolutePath());
                        updateMessage("");
                        updateProgress(0, 0);
                        return getTree();
                    }
                };
            }
        };
    }

    /**
     * construct a service for reading one or more files representing one sample
     * @param filesToRead files to read
     * @return a phylotree
     */
    public Service<PhyloTree> meganReadingService(File[] filesToRead){
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
