package edu.metagenomecomparison.presenter.selection;

import edu.metagenomecomparison.WindowController;
import edu.metagenomecomparison.model.ComparativeTreeNode;
import edu.metagenomecomparison.presenter.SplittableTabPane;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;

public class SelectionPresenter {
    private final TreeNodeSelectionModel selectionModel;
    private final WindowController controller;

    private final PhyloTree tree;

    private SplittableTabPane splittableTabPane;



    public SelectionPresenter(TreeNodeSelectionModel selectionModel, WindowController controller, PhyloTree tree, SplittableTabPane splittableTabPane) {
        this.selectionModel = selectionModel;
        this.controller = controller;
        this.tree = tree;
        this.splittableTabPane = splittableTabPane;
    }

    public void setUp() {
        //setup click on nodes
        for (Node node : tree.getNodesAsList()) {
            ComparativeTreeNode cNode = (ComparativeTreeNode) node;
            Circle circle = cNode.getCircle();
            circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                    if (!e.isControlDown()) {
                        if (selectionModel.isSelected(cNode))
                            selectionModel.clearSelection(cNode);
                        else
                            selectionModel.select(cNode);
                    }
                });
        }

        //setup listening to changes

        if (!selectionModel.isSetUp()) {
            selectionModel.getSelectedItems().addListener(new SetChangeListener<>() {
                @Override
                public void onChanged(Change<? extends ComparativeTreeNode> c) {
                    if (c.wasAdded()) {
                        Platform.runLater(() -> {
                            visualizeSelection((c.getElementAdded()));
                            if (selectionModel.getSelectedItems().size() == 1 ) {
                                Tab infoTab = new Tab("Selection Info");
                                TextArea textArea = new TextArea();
                                textArea.setText(selectionInfoString());
                                infoTab.setContent(textArea);
                                splittableTabPane.getTabs().add(infoTab);
                                splittableTabPane.getSelectionModel().select(0);
                                splittableTabPane.getSelectionModel().select(splittableTabPane.findTab("Main"));
                            } else {
                                Tab infoTab = splittableTabPane.findTab("Selection Info");
                                if (infoTab != null)
                                    ((TextArea) infoTab.getContent()).setText(selectionInfoString());
                            }
                        });
                    } else if (c.wasRemoved()) {
                        Platform.runLater(() -> {
                            visualizeDeselection(c.getElementRemoved());
                        });
                        if (selectionModel.getSelectedItems().isEmpty()) {
                            splittableTabPane.getTabs().remove(splittableTabPane.findTab("Selection Info"));
                        } else {
                            Tab infoTab = splittableTabPane.findTab("Selection Info");
                            if (infoTab != null) ((TextArea) infoTab.getContent()).setText(selectionInfoString());
                        }
                    }
                }
            });
            selectionModel.setSetUp(true);
        }

    }

    public String selectionInfoString(){
        StringBuilder builder = new StringBuilder();
        for (ComparativeTreeNode cNode : this.selectionModel.getSelectedItems()){
            builder.append(cNode.makeTooltip());
            builder.append("\n\n");
        }
        return builder.toString();
    }

    public void visualizeSelection(ComparativeTreeNode cNode) {
        cNode.getCircle().setStroke(Color.BLUE);
    }

    public void visualizeDeselection(ComparativeTreeNode cNode) {
        cNode.getCircle().setStroke(null);
    }

    public TreeNodeSelectionModel getSelectionModel() {
        return selectionModel;
    }

}
