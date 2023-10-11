package edu.metagenomecomparison.model;

import edu.metagenomecomparison.presenter.selection.TreeNodeSelectionModel;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ExportData {
    public static void exportSelection(TreeNodeSelectionModel selectionModel, String[] openSamples, Window window){
        FileChooser fileChooser = new FileChooser();
        File newFile = fileChooser.showSaveDialog(window);
        Platform.runLater(() -> {
            try {
                BufferedWriter builder = new BufferedWriter(new FileWriter(newFile));
                builder.append("TaxonPath\t");
                for (String sample : openSamples)
                    builder.append(sample).append("\t");
                builder.append("\n");
                for (ComparativeTreeNode cNode : selectionModel.getSelectedItems()) {
                    builder.append(traverseParentsCreateString(cNode));
                    builder.append("\t");
                    for (String sample : openSamples) {
                        builder.append(String.valueOf(cNode.getAssigned(cNode.getSampleId(sample))));
                        builder.append("\t");
                    }
                    builder.append("\n");
                }
                builder.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void exportVisible(PhyloTree tree, String[] openSamples, Window window){
        FileChooser fileChooser = new FileChooser();
        File newFile = fileChooser.showSaveDialog(window);
        Platform.runLater(() -> {
            try {
                BufferedWriter builder = new BufferedWriter(new FileWriter(newFile));
                builder.append("TaxonPath\t");
                for (String sample : openSamples)
                    builder.append(sample).append("\t");
                builder.append("\n");
                for (Node node : tree.getNodesAsList()) {
                    ComparativeTreeNode cNode = (ComparativeTreeNode) node;
                    if (cNode.isVisible()) {
                        builder.append(traverseParentsCreateString(cNode));
                        builder.append("\t");
                        for (String sample : openSamples) {
                            builder.append(String.valueOf(cNode.getAssigned(cNode.getSampleId(sample))));
                            builder.append("\t");
                        }
                        builder.append("\n");
                    }
                }
                builder.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static String traverseParentsCreateString(ComparativeTreeNode cNode){
        ArrayList<ComparativeTreeNode> list = new ArrayList<>();
        ComparativeTreeNode parent = cNode;
        while (parent != null){
            list.add(parent);
            if (parent.getRank() == TaxonRank.ROOT)
                break;
            parent = (ComparativeTreeNode) parent.getParent();
        }
        StringBuilder builder = new StringBuilder();
        Collections.reverse(list);
        for (ComparativeTreeNode acNode : list){
            builder.append(TaxonRank.taxonRankMapRev().get(acNode.getRank()));
            builder.append("__");
            builder.append(acNode.getLabel());
            builder.append(";");
        }
        return builder.toString();
    }
}
