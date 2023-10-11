package edu.metagenomecomparison.presenter;

import edu.metagenomecomparison.model.graphGroup.GraphGroup;
import edu.metagenomecomparison.model.graphGroup.MultipleAbundancyGraphGroup;
import edu.metagenomecomparison.model.graphGroup.PairwiseComparisonGraphGroup;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

//TODO something like bringing graphtab to main window?
public class GraphTab extends Tab {
    //TODO it might be necessary at some point to be able to set the GraphRepresentation
    private boolean isPariwise;

    private int id1;

    private int id2;

    private String title;

    private GraphGroup graphGroup;

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

    public void setUpShowLabelsMenu(){
        CheckMenuItem showLabels = new CheckMenuItem("Show labels");
        showLabels.setOnAction(e -> {
            if (!this.getTabContent().getChildren().isEmpty()){
                Group labels = (Group) ((Group) this.getTabContent().getChildren().get(0)).getChildren().get(2);
                labels.setVisible(true);
                for (var text : labels.getChildren()){
                    if (!(text instanceof Text))
                        return;
                    else{
                        ((Text) text).setFont(new Font(((Text) text).getFont().getSize() - 6));
                    }
                }
            }
        });
        if (this.getContextMenu() != null)
            this.getContextMenu().getItems().add(showLabels);
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

    public void setGraphGroup(GraphGroup graphGroup) {
        this.graphGroup = graphGroup;
    }

    public GraphGroup getGraphGroup() {
        return graphGroup;
    }

    public void repopulateContent(){
        getTabContent().getChildren().clear();
        if (isPariwise)
            addContent(((PairwiseComparisonGraphGroup) graphGroup).representationWithLogColorsBetween(id1, id2).
                    getAllLabelsInvisible());
        else
            addContent(((MultipleAbundancyGraphGroup) graphGroup).representationOfSampleId(id1).getAllLabelsInvisible());
    }


}
