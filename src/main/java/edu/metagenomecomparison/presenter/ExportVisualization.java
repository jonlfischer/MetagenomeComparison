package edu.metagenomecomparison.presenter;

import edu.metagenomecomparison.presenter.Coloring.ColorScaleLegendPane;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class ExportVisualization {
    /**
     * export a graph with open tabs. When you want to continue displaying, the representation and tab content has to be
     * drawn newly after using this function!
     * @param representation
     * @param tabs
     */
    public static Group exportMainWithTabs(GraphRepresentation representation
            , GraphTab[][] tabs, ColorScaleLegendPane colorScalePane){
        Group all = new Group();
        all.getChildren().addAll(representation.getAll(), colorScalePane);
        double maxX = representation.getAll().getBoundsInLocal().getMaxX() -
                representation.getAll().getBoundsInLocal().getMinX();
        double tabDeltaX = 0;
        boolean isTabInColumn = false;
        for (int i = 0; i < tabs.length; i++){
            double maxY = 0;
            if(isTabInColumn)
                maxX += tabDeltaX;
            isTabInColumn = false;
            for (int j = 0; j < tabs.length; j++){
                if (tabs[i][j] != null){
                    isTabInColumn = true;
                    Group tabContent = new Group(tabs[i][j].getTabContent().getChildren());
                    double tabMinY = tabContent.getBoundsInLocal().getMinY();
                    double tabMinX = tabContent.getBoundsInLocal().getMinX();
                    Text tabTitle = new Text(tabs[i][j].getTitle());
                    tabTitle.setFont(new Font(16));
                    tabTitle.setX(tabMinX);
                    tabTitle.setY(tabMinY + 20);
                    tabDeltaX =  tabContent.getBoundsInLocal().getMaxX() - tabMinX;
                    double tabDeltaY = tabContent.getBoundsInLocal().getMaxY() - tabMinY;
                    tabContent.getChildren().add(tabTitle);
                    tabContent.setTranslateX(- tabMinX + maxX);
                    tabContent.setTranslateY(- tabMinY + maxY);
                    all.getChildren().addAll(tabContent);
                    maxY += tabDeltaY;
                }
            }
        }
        return all;
    }




}
