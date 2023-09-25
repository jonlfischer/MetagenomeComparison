package edu.metagenomenewtest.presenter;

import jloda.fx.control.SplittableTabPane;
import javafx.scene.control.Tab;

public class SplittableTabUtils {
    /**
     * layout tabs from a matrix in a triangle like fashion, the way they are ordered in the upper right half of the matrix
     * @param tabs the tabs to layout
     * @param tabPane the splittable tab pane
     */
    public static void layoutTabsTriangle(Tab[][] tabs, SplittableTabPane tabPane){
        Tab mainTab = tabPane.getTabs().get(0);
        if (tabs[1][0] != null)
        tabPane.showRight(mainTab, tabs[1][0]);

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
        //TODO figure out a way to do this or make public in jloda
        //tabPane.moveTab(toShow, toShow.getTabPane(), reference.getTabPane());
        tabPane.showBelow(reference,toShow);
    }

    public static void layoutTabsSquare(Tab[] tabs, SplittableTabPane tabPane){
        if (tabs.length == 0) return;
        Tab mainTab = tabPane.getTabs().get(0);
        tabPane.showRight(mainTab, tabs[0]);
        int c = 1;
        int maxI = (int) Math.ceil(Math.sqrt(tabs.length));
        for (int i = 0; i < maxI; i++){
            for (int j = 0; j < maxI; j++) {
                if (c >= tabs.length)
                    break;
               if (tabs[c-1] != null && tabs[c] != null) certainlyMoveBelow(tabPane, tabs[c-1], tabs[c]);
               c++;
            }
            if (c >= tabs.length)
                break;
            if (tabs[c] != null) tabPane.showRight(mainTab, tabs[c]);
            c++;
        }

    }


}
