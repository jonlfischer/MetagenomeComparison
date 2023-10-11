package edu.metagenomecomparison.presenter.undoredo;

import edu.metagenomecomparison.WindowController;
import edu.metagenomecomparison.model.ComparativeTreeNode;

import edu.metagenomecomparison.presenter.selection.TreeNodeSelectionModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;

public class UndoAbleSetup {
    public static void setUpUndo(TreeNodeSelectionModel selectionModel, UndoRedoManager undoManager,
                                 WindowController controller, PhyloTree tree){
        controller.getUndoMenu().setOnAction(e -> undoManager.undo());
        controller.getUndoMenu().textProperty().bind(undoManager.undoLabelProperty());
        controller.getUndoMenu().disableProperty().bind(undoManager.canUndoProperty().not());
        controller.getRedoMenu().setOnAction(e -> undoManager.redo());
        controller.getRedoMenu().textProperty().bind(undoManager.redoLabelProperty());
        controller.getRedoMenu().disableProperty().bind(undoManager.canRedoProperty().not());
        selectionModel.getSelectedItems().addListener((SetChangeListener<ComparativeTreeNode>) change -> {
            if (change.wasAdded()) {
                undoManager.add(new SimpleCommand(
                        "select node",
                        () -> selectionModel.clearSelection(change.getElementAdded()),
                        () -> selectionModel.select(change.getElementAdded())
                ));
            } else {
                undoManager.add(new SimpleCommand(
                        "deselect node",
                        () -> selectionModel.select(change.getElementRemoved()),
                        () -> selectionModel.clearSelection(change.getElementRemoved())
                ));
            }
        });
        for(Node n: tree.getNodesAsList()){
            ComparativeTreeNode cNode = (ComparativeTreeNode) n;
            cNode.getChildrenVisibleProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                    undoManager.add(new SimpleCommand("Toggle children visibility of " + cNode.getLabel(),
                            cNode::toggleVisibility,
                            cNode::toggleVisibility));
                    //TODO this behaves unexpected when using the collapse below menu
                }
            });
        }

    }
}
