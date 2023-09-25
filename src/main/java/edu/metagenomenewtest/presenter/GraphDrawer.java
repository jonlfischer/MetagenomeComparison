package edu.metagenomenewtest.presenter;

import edu.metagenomenewtest.model.ComparativeTreeNode;
import edu.metagenomenewtest.model.GraphLayout;
import edu.metagenomenewtest.model.TaxonRank;
import jloda.graph.Graph;
import jloda.graph.Node;
import edu.metagenomenewtest.presenter.Coloring.ColorScale;
import edu.metagenomenewtest.presenter.Coloring.SaturationColorScale;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GraphDrawer {

    public static GraphRepresentation drawGraph(Graph graph, GraphLayout layout,
                                                Function<ComparativeTreeNode, Double> nodeToColorValue,
                                                ColorScale colorScale, double coordinateScalingFactor,
                                                boolean isMainGraph){
        Group nodeRepr = new Group();
        Group edgeRepr = new Group();
        Group labels = new Group();
        ComparativeTreeNode[] nodes = new ComparativeTreeNode[graph.getNumberOfNodes()];
        int idx = 0;
        for (Node v :  layout.getNodes()){
            nodes[idx] = (ComparativeTreeNode) v;
            idx ++;
        }
        int[][] edges = layout.getEdges();
        double[][] coordinates = layout.getCoordinates();
        Circle[] circles = new Circle[graph.getNumberOfNodes()];


        for (int i = 0; i < coordinates[0].length; i++){
            double x = coordinates[0][i]*coordinateScalingFactor;
            double y = coordinates[1][i]*coordinateScalingFactor;
            ComparativeTreeNode v = nodes[i];
            int countsBelow = v.getTotalSummed();
            Circle c = v.makeCircle(x, y, Math.pow(countsBelow, 0.2) * (coordinateScalingFactor / 20), isMainGraph);
            //Coloring
            if (colorScale != null && nodeToColorValue != null)
                c.setFill(colorScale.getValueColor(nodeToColorValue.apply(v)));
                //v.changeColor(colorScale.getValueColor(nodeToColorValue.apply(v)));
            else
                c.setFill(Color.hsb(0, 0, 0.7));
                //v.changeColor( Color.hsb(0, 0, 0.7));

            circles[i] = c;
            Tooltip t = new Tooltip();
            t.setText(v.makeTooltip());
            Tooltip.install(c, t);
            Text text = new Text(v.getLabel());
            //TODO set font size according to rank
            TaxonRank fontSizeRank = v.getRank();
            ComparativeTreeNode u = v;
            while (fontSizeRank == TaxonRank.NORANK){
                fontSizeRank = ((ComparativeTreeNode) u.getParent()).getRank();
                u = (ComparativeTreeNode) u.getParent();
            }
            text.setFont(new Font(TaxonRank.taxonRankToFontSize().get(fontSizeRank)));
            text.setX(x);
            text.setY(y);
            text.setX(text.getX() - text.getLayoutBounds().getWidth() / 2);
            text.setY(text.getY() + text.getLayoutBounds().getHeight() / 4);
            text.setStroke(Color.WHITE);
            text.setStrokeWidth(0.3);
            text.setFill(Color.BLACK);
            text.visibleProperty().bind(v.getCircle().visibleProperty());
            text.onMouseClickedProperty().bind(c.onMouseClickedProperty());

            text.opacityProperty().bind(new DoubleBinding() {
                {super.bind(text.opacityProperty(), c.fillProperty());}
                @Override
                protected double computeValue() {
                    return ((Color) c.fillProperty().get()).getOpacity();
                }
            });

            t.setStyle("-fx-font-size: 14");
            Tooltip.install(text, t);

            labels.getChildren().add(text);
            nodeRepr.getChildren().add(c);
            labels.setViewOrder(-1.0);
        }

        for (int j = 0; j < edges[0].length; j++){
            int source = edges[0][j];
            int target = edges[1][j];
            Line line = new Line();

            Circle sourceN = circles[source];
            Circle targetN = circles[target];

            line.startXProperty().bind(sourceN.centerXProperty());
            line.startYProperty().bind(sourceN.centerYProperty());

            line.endXProperty().bind(targetN.centerXProperty());
            line.endYProperty().bind(targetN.centerYProperty());

            line.setStrokeWidth(targetN.getRadius());
            line.strokeProperty().bind(targetN.fillProperty());

            line.visibleProperty().bind(Bindings.and(
                    sourceN.visibleProperty(),
                    targetN.visibleProperty()));
            edgeRepr.getChildren().add(line);
        }
    return new GraphRepresentation(nodeRepr, edgeRepr, labels);
    }







    //TODO this should definitely be more generic or somewhere else
    public static SaturationColorScale createLogAbundanciesColorScale(Graph graph) {
            List<Double> logAbundancies = new ArrayList<>();
            for (Node v : graph.nodes())
                logAbundancies.add(((ComparativeTreeNode) v).getLogAbundancies(0, 1, true));
            double min = logAbundancies.stream().mapToDouble(d -> d).min().orElse(Math.log((double) 1 / 100000));
            double max = logAbundancies.stream().mapToDouble(d -> d).max().orElse(Math.log(100000));
            System.out.println("min log abundancy " + min + "max " + max);
            double absMax = Math.max(Math.abs(min), Math.abs(max));
            return new SaturationColorScale(absMax, Color.TURQUOISE, Color.FIREBRICK);
    }



}
