package edu.metagenomecomparison.presenter;

import edu.metagenomecomparison.WindowController;
import edu.metagenomecomparison.model.*;
import edu.metagenomecomparison.model.graphGroup.MultipleTimelineGraphGroup;
import edu.metagenomecomparison.model.graphGroup.TimelineGraphGroup;
import edu.metagenomecomparison.presenter.Coloring.ColorScale;
import edu.metagenomecomparison.presenter.Coloring.ColorScaleLegendPane;
import edu.metagenomecomparison.presenter.Coloring.GradientColorScale;
import edu.metagenomecomparison.presenter.dialogs.SelectTabsDialog;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.dialog.ExportImageDialog;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

//TODO a feature would be a slider with a threshhold abundance for a node to be shown
public class WindowPresenter{
    private PhyloTree tree;
    private GraphRepresentation graphRepresentation;

    private Group labels;

    private ColorScaleLegendPane colorScaleLegend;

    private ProgressBar progressBar;
    private Label processLabel;

    private Pane mainPane;

    private VBox vBox;

    private Slider animationSlider;

    private CheckBox playCheckBox;

    private Timeline timeline = null;

    private SplittableTabPane tabPane;

    private GraphLayout layout;

    private Tab mainTab;

    //TODO update accordingly when tabs are closed and stuff
    private GraphTab[][] openTabs;

    private GraphTab[] openTabsSingle;

    enum Mode{
        SINGLE,
        MULTIPLE,
        TIMELINE
    }
    //TODO here and below: make labels scale dependent on zoom state

    private Mode currentMode;
    private ObjectProperty<Mode> modeProperty = new SimpleObjectProperty<>(currentMode);

    private WindowController controller;

    public WindowPresenter(WindowController controller){
        this.controller = controller;

        vBox = controller.getvBox();
        MenuItem singleFile = controller.getOpenSingle();
        MenuItem multipleFiles = controller.getOpenMultiple();
        MenuItem timeLineFiles = controller.getOpenTimelineFolder();
        MenuItem multipleComparison = controller.getOpenMultipleMegan();
        MenuItem timeLineComparison = controller.getOpenTimelineMegan();
        MenuItem secondTimelineFolderMenu = controller.getSecondTimelineFolderMenu();
        MenuItem secondTimelineComparisonMenu = controller.getSecondTimelineComparisonMenu();
        CheckMenuItem showLables = controller.getShowLabels();
        animationSlider = controller.getAnimationSlider();
        playCheckBox = controller.getPlayCheckBox();

        tabPane = new SplittableTabPane();
        tabPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        vBox.getChildren().add(1, tabPane);

        //TODO maybe anchorpane -> splittabletabpane -> anchorpane, currently colorscalelegend only moves in main tab
        AnchorPane mainAnchorPane = new AnchorPane();
        this.colorScaleLegend = new ColorScaleLegendPane(mainAnchorPane, 0, 0);

        mainPane = new Pane();
        ZoomableScrollPane scrollPane = new ZoomableScrollPane(mainPane);
        mainAnchorPane.getChildren().add(scrollPane);
        mainTab = new Tab("Main");
        tabPane.getTabs().add(mainTab);
        mainTab.setClosable(false);
        mainTab.setContent(mainAnchorPane);
        vBox.heightProperty().addListener((val, old, n) -> tabPane.setPrefHeight(n.doubleValue()));

        mainAnchorPane.heightProperty().addListener((val, old, n) -> scrollPane.setPrefHeight(n.doubleValue()));
        mainAnchorPane.widthProperty().addListener((val, old, n) -> scrollPane.setPrefWidth(n.doubleValue()));

        progressBar = controller.getProgressBar();
        processLabel = controller.getStatusLabel();


        singleFile.setOnAction(e -> readFile((Stage) vBox.getScene().getWindow()));

        multipleFiles.setOnAction(e -> {
            readMultipleFiles((Stage) vBox.getScene().getWindow());
        });

        multipleComparison.setOnAction(e-> readComparison((Stage) vBox.getScene().getWindow(),
                Mode.MULTIPLE));

        timeLineFiles.setOnAction(e -> readTimelineFolder((Stage) vBox.getScene().getWindow(), false));

        timeLineComparison.setOnAction(e -> readComparison((Stage) vBox.getScene().getWindow(),
                    Mode.TIMELINE));

        secondTimelineFolderMenu.setOnAction(k -> {
            readTimelineFolder((Stage) vBox.getScene().getWindow(), true);
        });

        controller.getIncreaseFontSizeMenu().setOnAction(q -> {
            for (Node node : this.labels.getChildren()){
                Text t = (Text) node;
                double fontsize = t.getFont().getSize() + 1;
                t.setFont(new Font(Math.min(fontsize, 30)));
                }});
        controller.getDecreaseFontSizeMenu().setOnAction(q -> {
            for (Node node : this.labels.getChildren()){
                Text t = (Text) node;
                double fontsize = t.getFont().getSize() - 1;
                t.setFont(new Font(Math.max(1, fontsize)));
            }});

        showLables.setOnAction(e -> labels.setVisible(showLables.isSelected()));

        for (MenuItem rankToCollapse : controller.getCollapseNodesBelow().getItems()){
            rankToCollapse.setOnAction(l -> {
                TaxonRank rank = TaxonRank.taxonRankMap().get(rankToCollapse.getId());
                System.out.println(rank.toString());
                Util.collapseBelow(this.tree, rank);
            });
        }

        controller.getUncollapseAll().setOnAction(u -> Util.uncollapseAll(tree));

        controller.getSelectTabsToShowMenu().disableProperty().bind(modeProperty.isNotEqualTo(Mode.MULTIPLE));

        controller.getExportMainMenu().setOnAction(q -> ExportImageDialog.show("main", (Stage) vBox.getScene().getWindow(),
                graphRepresentation.getAll()));

        controller.getExportOpenTabsMenu().setOnAction(q -> {
            ExportImageDialog.show("mainWithTabs",
                    (Stage) vBox.getScene().getWindow(),
                    Export.exportMainWithTabs(graphRepresentation,
                            openTabs,
                            this.colorScaleLegend));
            this.graphRepresentation = GraphDrawer.drawGraph(tree, layout, null,
                    null, 20, true);
            this.labels = this.graphRepresentation.getLabels();
            reloadGraph(mainPane);
            //TODO could also open tabs again here using openTabs
            tabPane.getTabs().retainAll(mainTab);
        });

        controller.getSelectTabsToShowMenu().setOnAction(p -> {
            HashMap<String, Integer> readSampleFileNames = ((ComparativeTreeNode) this.tree.getRoot()).getSampleNameToId();
            String[] files = readSampleFileNames.keySet().toArray(new String[0]);
            SelectTabsDialog dialog = new SelectTabsDialog(files, vBox.getScene().getWindow());
                    GraphTab[][] toShowTabs = dialog.showAndWait().get();
                    GraphTab[] toShowUnstacked;
                    boolean isPairwise;
                    if (toShowTabs.length > 1) {
                        isPairwise = true;
                        toShowUnstacked = SplittableTabUtils.populatePairwiseTabs(tree, layout, toShowTabs,
                                files, this.colorScaleLegend);
                        openTabs = toShowTabs;
                    }
                    else if (toShowTabs.length == 1){
                        isPairwise = false;
                        toShowUnstacked = SplittableTabUtils.populateSingleTabs(tree, layout, toShowTabs,
                                files, this.colorScaleLegend);
                    }
                    else return;

                    tabPane.getTabs().retainAll(mainTab);
                    for (Tab tabToAdd : toShowUnstacked)
                        if (tabToAdd != null) tabPane.getTabs().add(tabToAdd);
                    if (isPairwise) SplittableTabUtils.layoutTabsTriangle(toShowTabs, tabPane);
                    else openTabs = SplittableTabUtils.layoutTabsSquare(toShowUnstacked, tabPane);
                }
        );

    }


    private void reloadGraph(Pane pane){
        pane.getChildren().clear();
        if (graphRepresentation.getAll().getBoundsInLocal().getMinX() < 0)
            graphRepresentation.getAll().setTranslateX(-graphRepresentation.getAll().getBoundsInLocal().getMinX());
        if (graphRepresentation.getAll().getBoundsInLocal().getMinY() < 0)
            graphRepresentation.getAll().setTranslateY(-graphRepresentation.getAll().getBoundsInLocal().getMinY());
        mainPane.getChildren().addAll(graphRepresentation.getAll());
        this.labels = graphRepresentation.getLabels();
    }

    private void startReadingService(Service<PhyloTree> meganReadingService, Mode mode){
        progressBar.progressProperty().bind(meganReadingService.progressProperty());
        processLabel.textProperty().bind(meganReadingService.messageProperty());
        meganReadingService.setOnSucceeded(p -> {
                    this.tree = meganReadingService.getValue();
                    modeProperty.set(mode);
                    onFinishedParsing(mode, false);
                }
        );
        meganReadingService.start();
    }

    private void readComparison(Stage stage, Mode mode){
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(stage);
        MeganPathCountParser parser = new MeganPathCountParser();
        Service<PhyloTree> meganReadingService = parser.meganReadingService(file);
        startReadingService(meganReadingService, mode);
    }


    private String readFile(Stage stage){
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(stage);
        readFileS(new File[]{file}, Mode.SINGLE, false);
        return file.getName();
    }

    private List<File> readMultipleFiles(Stage stage){
        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        readFileS(files.toArray(new File[0]), Mode.MULTIPLE, false);
        return files;
    }

    private void readTimelineFolder(Stage stage, boolean isAnotherTimline){
        DirectoryChooser chooser = new DirectoryChooser();
        File[] files = chooser.showDialog(stage).listFiles();
        readFileS(Util.orderFilesByTime(files), Mode.TIMELINE, isAnotherTimline);
    }


    private void readFileS(File[] filesToRead, Mode mode, boolean isAnotherTimeline){
        MeganPathCountParser parser = new MeganPathCountParser();
        if (isAnotherTimeline && mode == Mode.TIMELINE){
            parser.setTree(tree);
        }
        Service<PhyloTree> meganReadingService = parser.meganReadingService(filesToRead);
        startReadingService(meganReadingService, mode);
        //TODO i do not know why this isnt working, either obj or min is null
        /*
        meganReadingService.valueProperty().addListener(new ChangeListener<PhyloTree>() {
            @Override
            public void changed(ObservableValue<? extends PhyloTree> observableValue, PhyloTree oldTree, PhyloTree newTree) {
                if (observableValue.getValue() != null) {
                    tree = observableValue.getValue();
                    NodeArray<Point2D> nodePoints = LayoutTreeRadial.apply(tree);
                    GraphLayout newLayout = GraphLayout.fromNodeArray(tree, nodePoints);
                    graphRepresentation = GraphDrawer.drawGraph(tree, newLayout, null, null, 100);
                    labels = graphRepresentation.getLabels();
                    reloadGraph(pane);
                    System.out.println("tree change");
                }
            }
        });
         */

    }

    public void onFinishedParsing(Mode mode, boolean isAnotherTimeline){
        HashMap<String, Integer> readSampleFileNames = ((ComparativeTreeNode) this.tree.getRoot()).getSampleNameToId();
        this.tabPane.getTabs().retainAll(mainTab);
        if (this.timeline != null) {
            timeline.stop();
            playCheckBox.setSelected(false);
            timeline = null;
        }
        if (mode == Mode.SINGLE){
            this.colorScaleLegend.clear();
            layoutFunction(null, null,
                    mainPane).start();
        }
        else if (mode == Mode.MULTIPLE){
            //TODO this doesnt preserve orderuing
            String[] files = readSampleFileNames.keySet().toArray(new String[0]);
            ColorScale colorScale;
            if (readSampleFileNames.keySet().size() == 2) {
                colorScale = GraphDrawer.createLogAbundanciesColorScale(this.tree);
                layoutFunction(comparativeTreeNode -> comparativeTreeNode.getLogAbundancies(0, 1,
                                true), colorScale, mainPane).start();
                this.colorScaleLegend.setContent(colorScale.createColorScaleImage(120, 300,
                        Orientation.VERTICAL,
                        "log abundancies", files[0], files[1]));
            } else { //TODO make this into a service
                Service<GraphLayout> dhService = layoutFunction(null, null, mainPane);
                dhService.setOnSucceeded(l -> {
                            layout = dhService.getValue();
                            this.graphRepresentation = GraphDrawer.drawGraph(tree, layout, null,
                                    null, 20, true);
                            this.labels = this.graphRepresentation.getLabels();
                            reloadGraph(mainPane);

                        });
                dhService.start();

            }
        }
        else if (mode == Mode.TIMELINE){
            TimelineGraphGroup graphGroup;
            GradientColorScale colorScale;
            if (!isAnotherTimeline) {
                graphGroup = new TimelineGraphGroup(tree);
            }
            else {
                //TODO when creating multipletimelinegraphgroup it always needs to be checked if numSamples of new tree is 2x numsamples of old graphgroup
                //TODO or think of another way to do all this like open multiple timelines at once
                graphGroup = new MultipleTimelineGraphGroup(tree);
            }
            colorScale = graphGroup.createColorScale(Color.YELLOW, Color.RED);
            Service<GraphLayout> dhService = layoutFunction(
                    comparativeTreeNode -> comparativeTreeNode.getRelativeSummed(0),
                    colorScale,
                    mainPane);
            dhService.setOnSucceeded(l -> {
                GraphLayout layout = dhService.getValue();
                this.graphRepresentation = GraphDrawer.drawGraph(tree, layout,
                        comparativeTreeNode -> comparativeTreeNode.getRelativeSummed(0), colorScale,
                        20, true);
                this.labels = this.graphRepresentation.getLabels();
                reloadGraph(mainPane);
                timeline = TimelineGraphAnimation.setUpAnimation(graphGroup, animationSlider, playCheckBox);
            });
            dhService.start();
            colorScaleLegend.setContent(colorScale.createColorScaleImage(120, 300,
                    Orientation.VERTICAL,
                    "relative Abundancies", "", ""));

        }
    }


    private Service<GraphLayout> layoutFunction(Function<ComparativeTreeNode, Double> nodeToColorValue,
                                ColorScale colorScale, Pane pane){
        NodeArray<Point2D> nodePoints = LayoutTreeRadial.apply(tree);
        DavidsonHarel dh = new DavidsonHarel(tree, nodePoints);
        //dh.layoutWithDH(0.65, 20, 15);
        Service<GraphLayout> dhService = dh.davidsonHarelService();
        progressBar.progressProperty().bind(dhService.progressProperty());
        processLabel.textProperty().bind(dhService.messageProperty());
        dhService.valueProperty().addListener(new ChangeListener<GraphLayout>() {
            @Override
            public void changed(ObservableValue<? extends GraphLayout> observableValue, GraphLayout layout, GraphLayout newLayout) {
                graphRepresentation = GraphDrawer.drawGraph(tree, newLayout, nodeToColorValue, colorScale,
                        40, true);
                labels = graphRepresentation.getLabels();
                reloadGraph(pane);
            }
        });
        dhService.setOnSucceeded(i -> {
            layout = dhService.getValue();
            this.graphRepresentation = GraphDrawer.drawGraph(tree, layout, nodeToColorValue, colorScale,
                    20, true);
            this.labels = this.graphRepresentation.getLabels();
            reloadGraph(pane);


        });
        return dhService;
    }

}
