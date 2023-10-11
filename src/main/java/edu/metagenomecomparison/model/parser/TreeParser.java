package edu.metagenomecomparison.model.parser;

import edu.metagenomecomparison.model.ComparativeTreeNode;
import javafx.concurrent.Service;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class TreeParser {

    int numReadFiles;
    PhyloTree tree;
    ArrayList<String> readSamples;


    public abstract Service<PhyloTree> readingService(File... files);


    /**
     * get the tree constructed from the parser. can be called after multiple calls to read for shared tree
     * @return the tree
     */
    public PhyloTree getTree() {
        return tree;
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
}
