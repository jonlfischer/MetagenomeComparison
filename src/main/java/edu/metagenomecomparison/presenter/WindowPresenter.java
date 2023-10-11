package edu.metagenomecomparison.presenter;

import edu.metagenomecomparison.WindowController;
import edu.metagenomecomparison.model.*;
import edu.metagenomecomparison.model.graphGroup.MultipleTimelineGraphGroup;
import edu.metagenomecomparison.model.graphGroup.TimelineGraphGroup;
import edu.metagenomecomparison.model.ComparativeTreeNode;
import edu.metagenomecomparison.model.layout.DavidsonHarel;
import edu.metagenomecomparison.model.layout.GraphLayout;
import edu.metagenomecomparison.model.layout.LayoutTreeRadial;
import edu.metagenomecomparison.model.parser.FeatureTableParser;
import edu.metagenomecomparison.model.parser.MeganComparisonFileParser;
import edu.metagenomecomparison.model.parser.MeganSingleFileParser;
import edu.metagenomecomparison.model.parser.TreeParser;
import edu.metagenomecomparison.presenter.Coloring.ColorScale;
import edu.metagenomecomparison.presenter.Coloring.ColorScaleLegendPane;
import edu.metagenomecomparison.presenter.Coloring.GradientColorScale;
import edu.metagenomecomparison.presenter.dialogs.SelectTabsDialog;
import edu.metagenomecomparison.presenter.selection.SelectionPresenter;
import edu.metagenomecomparison.presenter.selection.TreeNodeSelectionModel;
import edu.metagenomecomparison.presenter.undoredo.UndoAbleSetup;
import edu.metagenomecomparison.presenter.undoredo.UndoRedoManager;
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

    private TreeNodeSelectionModel selectionModel;

    private SelectionPresenter selectionPresenter;

    private HashMap<String, Integer> readSampleFileNames;

    enum Mode{
        SINGLE,
        MULTIPLE,
        TIMELINE
    }
    //TODO here and below: make labels scale dependent on zoom state

    private ObjectProperty<Mode> modeProperty = new SimpleObjectProperty<>(null);

    private WindowController controller;

    private UndoRedoManager undoRedoManager;

    public WindowPresenter(WindowController controller){
        this.controller = controller;
        this.undoRedoManager = new UndoRedoManager();

        vBox = controller.getvBox();
        MenuItem singleFile = controller.getOpenSingle();
        MenuItem multipleFiles = controller.getOpenMultiple();
        MenuItem timeLineFiles = controller.getOpenTimelineFolder();
        MenuItem multipleComparison = controller.getOpenMultipleMegan();
        MenuItem timeLineComparison = controller.getOpenTimelineMegan();

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


        singleFile.setOnAction(e -> readFile((Stage) vBox.getScene().getWindow(), new MeganSingleFileParser()));

        multipleFiles.setOnAction(e -> {
            readMultipleFiles((Stage) vBox.getScene().getWindow(), new MeganSingleFileParser());
        });

        multipleComparison.setOnAction(e-> readComparison((Stage) vBox.getScene().getWindow(),
                Mode.MULTIPLE, new MeganComparisonFileParser()));

        timeLineFiles.setOnAction(e -> readTimelineFolder((Stage) vBox.getScene().getWindow(), new MeganSingleFileParser()));

        timeLineComparison.setOnAction(e -> readComparison((Stage) vBox.getScene().getWindow(),
                    Mode.TIMELINE, new MeganComparisonFileParser()));

        controller.getOpenMultipleFeature().setOnAction(e -> readComparison((Stage) vBox.getScene().getWindow(),
                Mode.MULTIPLE, new FeatureTableParser()));


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
                Util.collapseBelow(this.tree, rank);
            });
        }

        controller.getUncollapseAll().setOnAction(u -> Util.uncollapseAll(tree));

        controller.getSelectAllMenu().setOnAction(e -> selectionModel.selectAll(tree.getNodesAsList()));

        controller.getUnselectAllMenu().setOnAction(e -> selectionModel.clearSelection());

        controller.getSelectTabsToShowMenu().disableProperty().bind(modeProperty.isNotEqualTo(Mode.MULTIPLE));

        controller.getDoDarkMode().selectedProperty().addListener((v, o, n) -> {
            if (n)
                vBox.getScene().getStylesheets().add("/edu/metagenomecomparison/dark-theme.css");
            else
                vBox.getScene().getStylesheets().remove("/edu/metagenomecomparison/dark-theme.css");
        });

        controller.getReopenSelectionInfoMenu().setOnAction(i -> {
            if (tabPane.findTab("Selection Info") == null){
                Tab newTab = new Tab("Selection Info");
                TextArea textArea = new TextArea();
                textArea.setText(selectionPresenter.selectionInfoString());
                newTab.setContent(textArea);
                newTab.setClosable(true);
                tabPane.getTabs().add(newTab);
                tabPane.getSelectionModel().select(0);
                tabPane.getSelectionModel().select(tabPane.findTab("Main"));
            }
        });

        controller.getExportSelectedMenu().setOnAction(e -> {
            ExportData.exportSelection(selectionModel,
                    readSampleFileNames.keySet().toArray(new String[0]),
                    vBox.getScene().getWindow());        });

        controller.getExportVisibleMenu().setOnAction(e -> {
            ExportData.exportVisible(tree, readSampleFileNames.keySet().toArray(new String[0]),
                    vBox.getScene().getWindow());        });

        controller.getExportMainMenu().setOnAction(q -> ExportImageDialog.show("main", (Stage) vBox.getScene().getWindow(),
                graphRepresentation.getAll()));

        controller.getExportOpenTabsMenu().setOnAction(q -> {
            ExportImageDialog.show("mainWithTabs",
                    (Stage) vBox.getScene().getWindow(),
                    ExportVisualization.exportMainWithTabs( graphRepresentation,
                            openTabs,
                            this.colorScaleLegend));
            this.graphRepresentation = GraphDrawer.drawGraph(tree, layout, null,
                    null, 20, true);
            this.labels = this.graphRepresentation.getLabels();
            reloadGraph(mainPane);
            SplittableTabUtils.repopulateTabs(openTabs);
        });

        controller.getSelectTabsToShowMenu().setOnAction(p -> {
            readSampleFileNames = ((ComparativeTreeNode) this.tree.getRoot()).getSampleNameToId();
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

                    tabPane.getTabs().retainAll(mainTab, tabPane.findTab("Selection Info"));
                    for (GraphTab tabToAdd : toShowUnstacked) {
                        if (tabToAdd != null) {
                            tabPane.getTabs().add(tabToAdd);
                            tabToAdd.setUpShowLabelsMenu();
                        }
                    }
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
        selectionModel = new TreeNodeSelectionModel();
        selectionPresenter = new SelectionPresenter(selectionModel, controller, tree, tabPane);
        selectionPresenter.setUp();
        UndoAbleSetup.setUpUndo(selectionModel, undoRedoManager, controller, tree);
    }

    private void startReadingService(Service<PhyloTree> treeReadingService, Mode mode){
        progressBar.progressProperty().bind(treeReadingService.progressProperty());
        processLabel.textProperty().bind(treeReadingService.messageProperty());
        treeReadingService.setOnSucceeded(p -> {
                    this.tree = treeReadingService.getValue();
                    modeProperty.set(mode);

                    onFinishedParsing(mode, false);
                }
        );
        treeReadingService.start();
    }

    private void readComparison(Stage stage, Mode mode, TreeParser parser){
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(stage);
        Service<PhyloTree> readingService = parser.readingService(file);
        startReadingService(readingService, mode);
    }


    private String readFile(Stage stage, TreeParser parser){
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(stage);
        readFileS(new File[]{file}, Mode.SINGLE, parser);
        return file.getName();
    }

    private List<File> readMultipleFiles(Stage stage, TreeParser parser){
        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        readFileS(files.toArray(new File[0]), Mode.MULTIPLE, parser);
        return files;
    }

    private void readTimelineFolder(Stage stage, TreeParser parser){
        DirectoryChooser chooser = new DirectoryChooser();
        File[] files = chooser.showDialog(stage).listFiles();
        readFileS(Util.orderFilesByTime(files), Mode.TIMELINE, parser);
    }


    private void readFileS(File[] filesToRead, Mode mode, TreeParser parser){
        Service<PhyloTree> readingService = parser.readingService(filesToRead);
        startReadingService(readingService, mode);
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
        readSampleFileNames = ((ComparativeTreeNode) this.tree.getRoot()).getSampleNameToId();
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
