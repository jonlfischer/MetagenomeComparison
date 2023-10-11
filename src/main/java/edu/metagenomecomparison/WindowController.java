package edu.metagenomecomparison;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class WindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Slider animationSlider;

    @FXML
    private GridPane bottomGrid;

    @FXML
    private ToolBar bottomToolBar;

    @FXML
    private Menu collapseNodesBelow;

    @FXML
    private MenuItem decreaseFontSizeMenu;

    @FXML
    private CheckMenuItem doDarkMode;

    @FXML
    private MenuItem exportMainMenu;

    @FXML
    private MenuItem exportOpenTabsMenu;

    @FXML
    private MenuItem exportSelectedMenu;

    @FXML
    private MenuItem exportTimelineMenu;

    @FXML
    private MenuItem exportVisibleMenu;

    @FXML
    private MenuItem increaseFontSizeMenu;

    @FXML
    private MenuItem openMultiple;

    @FXML
    private MenuItem openMultipleFeature;

    @FXML
    private MenuItem openMultipleMegan;

    @FXML
    private MenuItem openSingle;

    @FXML
    private MenuItem openTimelineFolder;

    @FXML
    private MenuItem openTimelineMegan;

    @FXML
    private CheckBox playCheckBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private MenuItem redoMenu;

    @FXML
    private MenuItem reopenSelectionInfoMenu;

    @FXML
    private MenuItem secondTimelineComparisonMenu;

    @FXML
    private MenuItem secondTimelineFolderMenu;

    @FXML
    private MenuItem selectAllMenu;

    @FXML
    private MenuItem selectTabsToShowMenu;

    @FXML
    private MenuItem settingsMenu;

    @FXML
    private CheckMenuItem showLabels;

    @FXML
    private Label statusLabel;

    @FXML
    private MenuItem uncollapseAll;

    @FXML
    private MenuItem undoMenu;

    @FXML
    private MenuItem unselectAllMenu;

    @FXML
    private VBox vBox;

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public Slider getAnimationSlider() {
        return animationSlider;
    }

    public GridPane getBottomGrid() {
        return bottomGrid;
    }

    public ToolBar getBottomToolBar() {
        return bottomToolBar;
    }

    public Menu getCollapseNodesBelow() {
        return collapseNodesBelow;
    }

    public MenuItem getDecreaseFontSizeMenu() {
        return decreaseFontSizeMenu;
    }

    public CheckMenuItem getDoDarkMode() {
        return doDarkMode;
    }

    public MenuItem getExportMainMenu() {
        return exportMainMenu;
    }

    public MenuItem getExportOpenTabsMenu() {
        return exportOpenTabsMenu;
    }

    public MenuItem getExportSelectedMenu() {
        return exportSelectedMenu;
    }

    public MenuItem getExportTimelineMenu() {
        return exportTimelineMenu;
    }

    public MenuItem getExportVisibleMenu() {
        return exportVisibleMenu;
    }

    public MenuItem getIncreaseFontSizeMenu() {
        return increaseFontSizeMenu;
    }

    public MenuItem getOpenMultiple() {
        return openMultiple;
    }

    public MenuItem getOpenMultipleFeature() {
        return openMultipleFeature;
    }

    public MenuItem getOpenMultipleMegan() {
        return openMultipleMegan;
    }

    public MenuItem getOpenSingle() {
        return openSingle;
    }

    public MenuItem getOpenTimelineFolder() {
        return openTimelineFolder;
    }

    public MenuItem getOpenTimelineMegan() {
        return openTimelineMegan;
    }

    public CheckBox getPlayCheckBox() {
        return playCheckBox;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public MenuItem getRedoMenu() {
        return redoMenu;
    }

    public MenuItem getReopenSelectionInfoMenu() {
        return reopenSelectionInfoMenu;
    }

    public MenuItem getSecondTimelineComparisonMenu() {
        return secondTimelineComparisonMenu;
    }

    public MenuItem getSecondTimelineFolderMenu() {
        return secondTimelineFolderMenu;
    }

    public MenuItem getSelectAllMenu() {
        return selectAllMenu;
    }

    public MenuItem getSelectTabsToShowMenu() {
        return selectTabsToShowMenu;
    }

    public MenuItem getSettingsMenu() {
        return settingsMenu;
    }

    public CheckMenuItem getShowLabels() {
        return showLabels;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public MenuItem getUncollapseAll() {
        return uncollapseAll;
    }

    public MenuItem getUndoMenu() {
        return undoMenu;
    }

    public MenuItem getUnselectAllMenu() {
        return unselectAllMenu;
    }

    public VBox getvBox() {
        return vBox;
    }

    @FXML
    void initialize() {
        assert animationSlider != null : "fx:id=\"animationSlider\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert bottomGrid != null : "fx:id=\"bottomGrid\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert bottomToolBar != null : "fx:id=\"bottomToolBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert collapseNodesBelow != null : "fx:id=\"collapseNodesBelow\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert decreaseFontSizeMenu != null : "fx:id=\"decreaseFontSizeMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert doDarkMode != null : "fx:id=\"doDarkMode\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportMainMenu != null : "fx:id=\"exportMainMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportOpenTabsMenu != null : "fx:id=\"exportOpenTabsMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportSelectedMenu != null : "fx:id=\"exportSelectedMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportTimelineMenu != null : "fx:id=\"exportTimelineMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportVisibleMenu != null : "fx:id=\"exportVisibleMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert increaseFontSizeMenu != null : "fx:id=\"increaseFontSizeMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMultiple != null : "fx:id=\"openMultiple\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMultipleFeature != null : "fx:id=\"openMultipleFeature\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMultipleMegan != null : "fx:id=\"openMultipleMegan\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openSingle != null : "fx:id=\"openSingle\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openTimelineFolder != null : "fx:id=\"openTimelineFolder\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openTimelineMegan != null : "fx:id=\"openTimelineMegan\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert playCheckBox != null : "fx:id=\"playCheckBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert redoMenu != null : "fx:id=\"redoMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert reopenSelectionInfoMenu != null : "fx:id=\"reopenSelectionInfoMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert secondTimelineComparisonMenu != null : "fx:id=\"secondTimelineComparisonMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert secondTimelineFolderMenu != null : "fx:id=\"secondTimelineFolderMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectAllMenu != null : "fx:id=\"selectAllMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectTabsToShowMenu != null : "fx:id=\"selectTabsToShowMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert settingsMenu != null : "fx:id=\"settingsMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showLabels != null : "fx:id=\"showLabels\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert uncollapseAll != null : "fx:id=\"uncollapseAll\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert undoMenu != null : "fx:id=\"undoMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert unselectAllMenu != null : "fx:id=\"unselectAllMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert vBox != null : "fx:id=\"vBox\" was not injected: check your FXML file 'MainWindow.fxml'.";

    }

}
