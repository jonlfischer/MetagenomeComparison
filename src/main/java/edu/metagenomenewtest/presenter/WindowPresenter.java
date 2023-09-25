package edu.metagenomenewtest.presenter;

import edu.metagenomenewtest.WindowController;
import edu.metagenomenewtest.model.*;
import javafx.scene.layout.*;
import jloda.fx.control.SplittableTabPane;
import jloda.fx.dialog.ExportImageDialog;
import jloda.graph.NodeArray;
import edu.metagenomenewtest.model.graphGroup.MultipleAbundancyGraphGroup;
import edu.metagenomenewtest.model.graphGroup.MultipleTimelineGraphGroup;
import edu.metagenomenewtest.model.graphGroup.PairwiseComparisonGraphGroup;
import edu.metagenomenewtest.model.graphGroup.TimelineGraphGroup;
import jloda.phylo.PhyloTree;
import edu.metagenomenewtest.presenter.Coloring.ColorScale;
import edu.metagenomenewtest.presenter.Coloring.ColorScaleLegendPane;
import edu.metagenomenewtest.presenter.Coloring.GradientColorScale;
import edu.metagenomenewtest.presenter.Coloring.SaturationColorScale;
import edu.metagenomenewtest.presenter.dialogs.SelectTabsDialog;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
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

        controller.getUncollapseAll().setOnAction(u -> {
            tree.preorderTraversal(tree.getRoot(), new Consumer<jloda.graph.Node>() {
                @Override
                public void accept(jloda.graph.Node node) {
                    ComparativeTreeNode cNode = (ComparativeTreeNode) node;
                    if (!cNode.isChildrenVisible() && cNode.isVisible())
                        cNode.toggleVisibility();
                }
            });
        });

        controller.getSelectTabsToShowMenu().disableProperty().bind(modeProperty.isNotEqualTo(Mode.MULTIPLE));

        controller.getExportMainMenu().setOnAction(q -> ExportImageDialog.show("main", (Stage) vBox.getScene().getWindow(),
                graphRepresentation.getAll()));

        controller.getSelectTabsToShowMenu().setOnAction(p -> {
            HashMap<String, Integer> readSampleFileNames = ((ComparativeTreeNode) this.tree.getRoot()).getSampleNameToId();
            String[] files = readSampleFileNames.keySet().toArray(new String[0]);
            SelectTabsDialog dialog = new SelectTabsDialog(files, vBox.getScene().getWindow());
                    GraphTab[][] toShowTabs = dialog.showAndWait().get();
                    Tab[] toShowUnstacked;
                    boolean isPairwise;
                    if (toShowTabs.length > 1) {
                        PairwiseComparisonGraphGroup graphGroup = new PairwiseComparisonGraphGroup(tree);
                        graphGroup.setLayout(layout);
                        SaturationColorScale saturationColorScale = graphGroup.createColorScale(Color.TURQUOISE, Color.FIREBRICK);
                        this.colorScaleLegend.setContent(saturationColorScale.createColorScaleImage(120, 200,
                                Orientation.VERTICAL, "log abundancies", "", ""));
                        isPairwise = true;
                        toShowUnstacked = new Tab[toShowTabs.length * toShowTabs.length];
                        int k = 0;
                        for (int i = 0; i < toShowTabs.length; i++) {
                            for (int j = 0; j < toShowTabs[0].length; j++) {
                                if (toShowTabs[i][j] != null) {
                                    int id1 = ((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().get(files[i]);
                                    int id2 = ((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().get(files[j]);
                                    toShowTabs[i][j].addContent(graphGroup.representationWithLogColorsBetween(id1, id2).getAllLabelsInvisible());
                                    toShowUnstacked[k] = toShowTabs[i][j];
                                    k++;
                                }
                            }
                        }
                    }
                    else if (toShowTabs.length == 1){
                        isPairwise = false;
                        MultipleAbundancyGraphGroup multipleAbundancyGraphGroup = new MultipleAbundancyGraphGroup(tree);
                        multipleAbundancyGraphGroup.setLayout(layout);
                        GradientColorScale gradientColorScale = multipleAbundancyGraphGroup.createColorScale(Color.YELLOW, Color.RED);
                        this.colorScaleLegend.setContent(gradientColorScale.createColorScaleImage(120, 200,
                                Orientation.VERTICAL, "relative abundancy", "", ""));
                        toShowUnstacked = new Tab[toShowTabs[0].length];
                        int c = 0;
                        for (int i = 0; i < toShowTabs[0].length; i++){
                            if(toShowTabs[0][i] != null) {
                                int id1 = ((ComparativeTreeNode) tree.getRoot()).getSampleNameToId().get(files[i]);
                                toShowTabs[0][i].addContent(multipleAbundancyGraphGroup.representationOfSampleId(id1).getAllLabelsInvisible());
                                toShowUnstacked[c] = toShowTabs[0][i];
                                c++;
                            }
                        }
                        toShowUnstacked = Arrays.copyOfRange(toShowUnstacked, 0, c);
                    }
                    else return;

                    //TODO figure out a way to do this or make public in jloda
                    //for (Tab tab : tabPane.getTabs()){
                        //if (tab != mainTab)
                            //tabPane.moveTab(tab, tab.getTabPane(), mainTab.getTabPane());
                    //}

                    tabPane.getTabs().retainAll(mainTab);
                    for (Tab tabToAdd : toShowUnstacked)
                        if (tabToAdd != null) tabPane.getTabs().add(tabToAdd); //TODO this seems to work but creates index oob exception
                    if(isPairwise) SplittableTabUtils.layoutTabsTriangle(toShowTabs, tabPane);
                    else SplittableTabUtils.layoutTabsSquare(toShowUnstacked, tabPane);
                    //TODO implement square layout for single samples
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
                timeline = setUpAnimation(graphGroup);
            });
            dhService.start();
            colorScaleLegend.setContent(colorScale.createColorScaleImage(120, 300,
                    Orientation.VERTICAL,
                    "relative Abundancies", "", ""));

        }
    }

    public Timeline setUpAnimation(TimelineGraphGroup graphGroup){
        int stepTimeMs = 5000;

        animationSlider.setMin(0);
        animationSlider.setMax(graphGroup.getNumSamples() - 1);
        Timeline timeline = TimelineGraphAnimation.createTimeline(graphGroup, stepTimeMs);
        reloadGraph(mainPane);

        timeline.setOnFinished(i -> timeline.playFromStart());
        animationSlider.valueProperty().addListener((observableValue, oldVal, newVal) ->
                timeline.jumpTo(Duration.millis(newVal.doubleValue() * stepTimeMs)));
        timeline.currentTimeProperty().addListener((observableValue, duration, newDur) ->
                animationSlider.setValue(newDur.toMillis() / stepTimeMs));
        animationSlider.setSnapToTicks(true);
        timeline.play(); timeline.pause();
        playCheckBox.setOnAction(l -> {
            if (playCheckBox.isSelected())
                timeline.play();
            else
                timeline.pause();
        });
        return timeline;
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
