package edu.metagenomecomparison.presenter.selection;

import edu.metagenomecomparison.model.ComparativeTreeNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import jloda.graph.Node;

import java.util.Collection;

public class TreeNodeSelectionModel {
    private final ObservableSet<ComparativeTreeNode> selectedItems = FXCollections.observableSet();

    private boolean isSetUp = false;

    public boolean isSetUp() {
        return isSetUp;
    }

    public void setSetUp(boolean setUp) {
        isSetUp = setUp;
    }

    public boolean isSelected(ComparativeTreeNode cNode){
        return selectedItems.contains(cNode);
    }
    public void select(ComparativeTreeNode cNode) {
        selectedItems.add(cNode);
    }

    public void setSelected(ComparativeTreeNode cNode, boolean select) {
        if (select) {
            select(cNode);
        } else {
            clearSelection(cNode);
        }
    }


    public void selectAll(Collection<? extends Node> list){
        for (Node n : list){
            ComparativeTreeNode cNode = (ComparativeTreeNode) n;
            selectedItems.add(cNode);
        }
    }
    public void clearSelection() {
        selectedItems.clear();
    }

    public void clearSelection(ComparativeTreeNode cNode) {
        selectedItems.remove(cNode);
    }

    public void clearSelection(Collection<ComparativeTreeNode> list) {
        selectedItems.removeAll(list);
    }

    public ObservableSet<ComparativeTreeNode> getSelectedItems() {
        return selectedItems;
    }

}
