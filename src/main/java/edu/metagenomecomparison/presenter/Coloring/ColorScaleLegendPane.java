package edu.metagenomecomparison.presenter.Coloring;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ColorScaleLegendPane extends Pane {
    private double mouseX = 0;
    private double mouseY = 0;

    public ColorScaleLegendPane(AnchorPane parent, double left, double top) {
        addToAnchorPane(parent, left, top);
        this.setViewOrder(-1);
        //this.setOpacity(0);
    }

    public void addToAnchorPane(AnchorPane anchorPane, double left, double top) {
        anchorPane.getChildren().add(this);
        AnchorPane.setLeftAnchor(this, left);
        AnchorPane.setTopAnchor(this, top);

        setOnMousePressed((e -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
        }));

        setOnMouseDragged((e -> {
            double deltaX = e.getScreenX() - mouseX;
            double deltaY = e.getScreenY() - mouseY;
            AnchorPane.setLeftAnchor(this, Math.max(5, AnchorPane.getLeftAnchor(this) + deltaX));
            AnchorPane.setTopAnchor(this, Math.max(5, AnchorPane.getTopAnchor(this) + deltaY));
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
        }));
    }

    public void setContent(StackPane colorScaleImage){
        this.getChildren().clear();
        this.getChildren().add(colorScaleImage);
    }

    public void clear(){
        this.getChildren().clear();
    }
}

