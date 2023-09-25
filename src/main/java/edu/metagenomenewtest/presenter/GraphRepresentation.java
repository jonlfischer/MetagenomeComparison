package edu.metagenomenewtest.presenter;

import javafx.scene.Group;
import javafx.scene.Node;

public class GraphRepresentation {
    private Group all;
    private Group nodeRepr;

    private Group labels;

    private Group edgeRepr;

    public GraphRepresentation(Group nodeRepr, Group edgeRepr, Group labels) {
        this.nodeRepr = nodeRepr;
        this.labels = labels;
        this.edgeRepr = edgeRepr;
        this.all = new Group( edgeRepr, nodeRepr, labels);
    }

    public Group getAll() {
        return all;
    }

    public Group getNodeRepr() {
        return nodeRepr;
    }

    public Group getLabels() {
        return this.labels;
    }

    public Group getEdgeRepr() {
        return edgeRepr;
    }

    public Group getAllLabelsInvisible() {
        labels.setVisible(false);
        return this.getAll();
    }

}
