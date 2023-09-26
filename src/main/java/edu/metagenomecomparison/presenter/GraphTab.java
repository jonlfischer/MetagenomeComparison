package edu.metagenomecomparison.presenter;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class GraphTab extends Tab {
    private boolean isPariwise;

    private int id1;

    private int id2;

    private String title;
    public GraphTab(String name){
        super(name);
        this.title = name;
        AnchorPane anchorPane = new AnchorPane();
        Pane pane = new Pane();
        ZoomableScrollPane scrollPane = new ZoomableScrollPane(pane);
        anchorPane.getChildren().add(scrollPane);
        //tabPane.getTabs().add(newTab);
        this.setClosable(true);
        this.setContent(anchorPane);
        anchorPane.heightProperty().addListener((val, old, n) -> scrollPane.setPrefHeight(n.doubleValue()));
        anchorPane.widthProperty().addListener((val, old, n) -> scrollPane.setPrefWidth(n.doubleValue()));
    }

    public void setIds(int id1){
        this.isPariwise = false;
        this.id1 = id1;
    }

    public void setIds(int id1, int id2){
        this.isPariwise = true;
        this.id1 = id1;
        this.id2 = id2;
    }

    public void addContent(Node... es){
        getTabContent().getChildren().addAll(es);
    }

    public boolean isPariwise() {
        return isPariwise;
    }

    public int getId1() {
        return id1;
    }

    public int getId2() {
        return id2;
    }

    public Pane getTabContent(){
        AnchorPane anchorPane = (AnchorPane) this.getContent();
        ZoomableScrollPane scrollPane = (ZoomableScrollPane) anchorPane.getChildren().get(0);
        return (Pane) scrollPane.getContentNode();
    }

    public static Pane getNonMainTabContent(Tab tab){
        AnchorPane anchorPane = (AnchorPane) tab.getContent();
        ZoomableScrollPane scrollPane = (ZoomableScrollPane) anchorPane.getChildren().get(0);
        return (Pane) scrollPane.getContentNode();
    }

    public String getTitle() {
        return title;
    }
}
