package edu.metagenomecomparison.presenter;

import edu.metagenomecomparison.model.ComparativeTreeNode;
import edu.metagenomecomparison.model.layout.GraphLayout;
import edu.metagenomecomparison.model.graphGroup.MultipleAbundancyGraphGroup;
import edu.metagenomecomparison.model.graphGroup.PairwiseComparisonGraphGroup;
import edu.metagenomecomparison.presenter.Coloring.ColorScaleLegendPane;
import edu.metagenomecomparison.presenter.Coloring.GradientColorScale;
import edu.metagenomecomparison.presenter.Coloring.SaturationColorScale;
import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import jloda.phylo.PhyloTree;

import java.util.Arrays;

public class SplittableTabUtils {
    /**
     * layout tabs from a matrix in a triangle like fashion, the way they are ordered in the upper right half of the matrix
     * @param tabs the tabs to layout
     * @param tabPane the splittable tab pane
     */
    public static void layoutTabsTriangle(Tab[][] tabs, SplittableTabPane tabPane){
        Tab mainTab = tabPane.getTabs().get(0);
        //if (tabs[1][0] != null)
          //  tabPane.showRight(mainTab, tabs[1][0]);

        for (int i = 1; i < tabs.length; i++) {
            if (tabs[i][0] != null) {
                tabPane.showRight(mainTab, tabs[i][0]);
                for (int j = 1; j < i; j++) {
                    if (tabs[i][j] != null)
                        certainlyMoveBelow(tabPane, tabs[i][0], tabs[i][j]);
                }
            }
        }
    }

    /**
     * move a tab below another tab, even if they are not in the same tabPane
     * @param tabPane
     * @param reference
     * @param toShow
     */
    public static void certainlyMoveBelow(SplittableTabPane tabPane, Tab reference, Tab toShow){
        tabPane.moveTab(toShow, toShow.getTabPane(), reference.getTabPane());
        tabPane.showBelow(reference,toShow);
    }

    public static GraphTab[][] layoutTabsSquare(GraphTab[] tabs, SplittableTabPane tabPane){
        if (tabs.length == 0) return new GraphTab[0][0];
        Tab mainTab = tabPane.getTabs().get(0);
        tabPane.showRight(mainTab, tabs[0]);
        int maxI = (int) Math.ceil(Math.sqrt(tabs.length));
        GraphTab[][] squareTabs = new GraphTab[maxI][maxI];
        for (int i = 0; i < maxI; i++){
            for (int j = 0; j < maxI; j++){
                if (j * maxI + i < tabs.length)
                    squareTabs[i][j] = tabs[j * maxI + i];
            }
        }
        for (int i = 0; i < maxI; i++){
            if (squareTabs[i][0] != null) {
                tabPane.showRight(mainTab, squareTabs[i][0]);
                for (int j = 1; j < maxI; j++) {
                    if (squareTabs[i][j] == null)
                        break;
                    certainlyMoveBelow(tabPane, squareTabs[i][0], squareTabs[i][j]);
                }
            }
        }
        return squareTabs;
    }

    public static GraphTab[] populatePairwiseTabs(PhyloTree tree, GraphLayout layout, GraphTab[][] toShowTabs,
                                                  String[] files, ColorScaleLegendPane colorScaleLegendPane){
        PairwiseComparisonGraphGroup graphGroup = new PairwiseComparisonGraphGroup(tree);
        graphGroup.setLayout(layout);
        SaturationColorScale saturationColorScale = graphGroup.createColorScale(Color.TURQUOISE, Color.FIREBRICK);
        colorScaleLegendPane.setContent(saturationColorScale.createColorScaleImage(120, 200,
                Orientation.VERTICAL, "log abundancies", "", ""));
        GraphTab[] toShowUnstacked = new GraphTab[toShowTabs.length * toShowTabs.length];
        int k = 0;
        for (int i = 0; i < toShowTabs.length; i++) {
            for (int j = 0; j < toShowTabs[0].length; j++) {
                if (toShowTabs[i][j] != null) {
                    int id1 = ((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().get(files[i]);
                    int id2 = ((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().get(files[j]);
                    toShowTabs[i][j].setGraphGroup(graphGroup);
                    toShowTabs[i][j].addContent(graphGroup.representationWithLogColorsBetween(id1, id2).getAllLabelsInvisible());
                    toShowUnstacked[k] = toShowTabs[i][j];
                    k++;
                }
            }
        }
        return toShowUnstacked;
    }

    public static GraphTab[] populateSingleTabs(PhyloTree tree, GraphLayout layout, GraphTab[][] toShowTabs,
                                                String[] files, ColorScaleLegendPane colorScaleLegendPane){
        MultipleAbundancyGraphGroup multipleAbundancyGraphGroup = new MultipleAbundancyGraphGroup(tree);
        multipleAbundancyGraphGroup.setLayout(layout);
        GradientColorScale gradientColorScale = multipleAbundancyGraphGroup.createColorScale(Color.YELLOW, Color.RED);
        colorScaleLegendPane.setContent(gradientColorScale.createColorScaleImage(120, 200,
                Orientation.VERTICAL, "relative abundancy", "", ""));
        GraphTab[] toShowUnstacked = new GraphTab[toShowTabs[0].length];
        int c = 0;
        for (int i = 0; i < toShowTabs[0].length; i++){
            if(toShowTabs[0][i] != null) {
                int id1 = ((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().get(files[i]);
                toShowTabs[0][i].setGraphGroup(multipleAbundancyGraphGroup);
                toShowTabs[0][i].addContent(multipleAbundancyGraphGroup.representationOfSampleId(id1).getAllLabelsInvisible());
                toShowUnstacked[c] = toShowTabs[0][i];
                c++;
            }
        }
        return Arrays.copyOfRange(toShowUnstacked, 0, c);
    }

    public static void repopulateTabs(GraphTab[][] tabs){
        for (int i = 0; i < tabs.length; i++){
            for(int j = 0; j< tabs.length; j++){
                tabs[i][j].repopulateContent();
            }
        }
    }


}
