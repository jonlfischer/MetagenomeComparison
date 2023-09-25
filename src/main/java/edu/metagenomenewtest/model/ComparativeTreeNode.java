package edu.metagenomenewtest.model;

import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Pair;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * a class for a tree that is constructed from multiple metagenomic samples
 */
public class ComparativeTreeNode extends Node {

    //key: number of sample, pair: assigned, summed reads
    private final HashMap<Integer, Pair<Integer, Integer>> data;

    public HashMap<String, Integer> getSampleNameToId() {
        return sampleNameToId;
    }

    private final HashMap<String, Integer> sampleNameToId;

    private boolean childrenVisible;

    public boolean isChildrenVisible() {
        return childrenVisible;
    }

    public Circle getCircle() {
        return circle;
    }

    private boolean isVisible;

    public boolean isVisible() {
        return isVisible;
    }

    private Circle circle;

    private int numSamples;

    private final HashMap<Integer, Integer> sampleIdToMaxSummed;

    private int totalAssigned;

    private int totalSummed;

    //TODO implement rank
    private TaxonRank rank;

    /**
     * sets the path in a tree to this node
     *
     * @param parentTaxonNames the path as taxon names
     */
    public void setParentTaxonNames(String[] parentTaxonNames) {
        this.parentTaxonNames = parentTaxonNames;
    }

    private String[] parentTaxonNames;

    public String[] getParentTaxonNames() {
        return parentTaxonNames;
    }

    public ComparativeTreeNode(Graph G) {
        super(G);
        data = new HashMap<>();
        sampleNameToId = new HashMap<>();
        sampleIdToMaxSummed = new HashMap<>();
        numSamples = 0;
        childrenVisible = true;
        isVisible = true;
    }

    private Pair<Integer, Integer> ZEROS() {
        Pair<Integer, Integer> zeros = new Pair<>();
        zeros.set(0, 0);
        return zeros;
    }

    /**
     * create a javafx circle representation of this node
     *
     * @param x      position x
     * @param y      position y
     * @param radius
     * @return the circle
     */
    public Circle makeCircle(double x, double y, double radius, boolean isMainGraph) {
        if (isMainGraph){
            circle = new Circle(x, y, radius);
            circle.setOnMouseClicked(e -> toggleVisibility());
            return circle;
        }
            else {
                Circle newCircle = new Circle(x, y, radius);
                newCircle.visibleProperty().bind(circle.visibleProperty());
                return newCircle;
        }
    }

    public void changeColor(Color color) {
        this.circle.setFill(color);
    }

    public void setRank(TaxonRank rank) {
        this.rank = rank;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    /**
     * make all nodes below this node invisible
     */
    public void toggleVisibility() {
        ComparativeTreeNode start = this;
        ((PhyloTree) this.getOwner()).preorderTraversal(start, new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                ComparativeTreeNode v = (ComparativeTreeNode) node;
                if (v != start) {
                    v.circle.setVisible(!start.childrenVisible);
                    v.isVisible = !v.isVisible;
                }
            }
        });
        this.childrenVisible = !childrenVisible;
    }

    /**
     * get a sampleId from its name (usually file name)
     *
     * @param sampleName the sample name
     * @return the sampleId
     */
    public int getSampleId(String sampleName) {
        return this.sampleNameToId.get(sampleName);
    }

    /**
     * set the number of reads assigned to this in sample with sample id
     *
     * @param sampleId the ID
     * @param count    the count assigned (not summarized)
     */
    public void setAssigned(int sampleId, int count) {
        data.putIfAbsent(sampleId, ZEROS());
        data.get(sampleId).setFirst(count);
    }

    /**
     * set the number of reads assigned to this in sample with id, including assigned below this. (summarized)
     *
     * @param sampleId the ID
     * @param count    the summarized count
     */
    public void setSummed(int sampleId, int count) {
        data.putIfAbsent(sampleId, ZEROS());
        data.get(sampleId).setSecond(count);
    }

    /**
     * get the number of reads assigned to this in sample with sample id
     *
     * @param sampleId the ID
     * @return the count assigned (not summarized)
     */
    public int getAssigned(int sampleId) {
        if (data.get(sampleId) == null) return 0;
        return data.get(sampleId).getFirst();
    }

    /**
     * get the number of reads assigned to this in sample with id, including assigned below this. (summarized)
     *
     * @param sampleId the ID
     * @return the summarized count
     */
    public int getSummed(int sampleId) {
        if (data.get(sampleId) == null) return 0;
        return data.get(sampleId).getSecond() + data.get(sampleId).getFirst();
    }

    /**
     * this is the sum of reads of the children without own assigned reads, which should be usually not used
     */
    protected int getSummedForTraversal(int sampleId) {
        if (data.get(sampleId) == null) return 0;
        return data.get(sampleId).getSecond();
    }

    public int getTotalAssigned() {
        return totalAssigned;
    }

    public int getTotalSummed() {
        return totalSummed;
    }

    public void setNumSamples(int numSamples, String sampleName, int rootSummed) {
        this.numSamples = numSamples;
        this.sampleNameToId.put(sampleName, numSamples);
        this.sampleIdToMaxSummed.put(numSamples, rootSummed);
        data.putIfAbsent(numSamples, ZEROS());
    }

    public void calculateTotalsFromScratch() {
        totalAssigned = 0;
        totalSummed = 0;
        for (int i = 0; i <= this.numSamples; i++) {
            double assigned = getAssigned(i);
            double summed = getSummed(i);
            totalAssigned += assigned;
            totalSummed += summed;
        }
    }

    /**
     * get the number of samples/files that the tree this node is in contains
     *
     * @return the number of samples / files
     */
    public int getNumSamples() {
        return numSamples + 1;
    }

    public Double getLogAbundancies(int sampleId1, int sampleId2, boolean summed) {
            double numerator = summed ? getSummed(sampleId1) : getAssigned(sampleId1);
            double denum = summed ? getSummed(sampleId2) : getAssigned(sampleId2);
            if (numerator == 0 && denum == 0) //TODO this should be a different case than when is present equally in both
                return null;
            if (numerator == 0)
                return Math.log((double) 1 / (1.2 * denum));
            else if (denum == 0)
                return Math.log(numerator * 1.2);
            else
                return Math.log(numerator / denum);
    }

    public String makeTooltip() {
        StringBuilder stringBuilder = new StringBuilder(this.getRank().toString() + ": ");
        for (String rank : this.parentTaxonNames)
            stringBuilder.append(rank).append(", ");
        stringBuilder.append("\n");
        stringBuilder.append("Total assigned reads: ").append(getTotalAssigned());
        stringBuilder.append("\n");
        stringBuilder.append("Total summarized reads: ").append(getTotalSummed());
        for (String key : this.sampleNameToId.keySet()) {
            int value;
            if (this.sampleNameToId.get(key) == null) value = 0;
            else value = this.sampleNameToId.get(key);
            stringBuilder.append("\n");
            stringBuilder.append("In file ").append(key);
            stringBuilder.append("\n");
            stringBuilder.append("Assigned reads: ").append(getAssigned(value));
            stringBuilder.append("\n");
            stringBuilder.append("Summarized reads: ").append(getSummed(value));
        }
        return stringBuilder.toString();
        //TODO include log abundancies if numsamples == 2
    }

    //TODO include relative summed and assigned probabilities

    public double getRelativeSummed(int sampleID) {
        return (double) getSummed(sampleID) / this.sampleIdToMaxSummed.get(sampleID);
    }

    public TaxonRank getRank() {
        return rank;
    }
}
