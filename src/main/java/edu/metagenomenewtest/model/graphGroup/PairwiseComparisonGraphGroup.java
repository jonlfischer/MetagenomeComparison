package edu.metagenomenewtest.model.graphGroup;

import edu.metagenomenewtest.model.ComparativeTreeNode;
import jloda.graph.Graph;
import jloda.graph.Node;
import edu.metagenomenewtest.presenter.Coloring.SaturationColorScale;
import edu.metagenomenewtest.presenter.GraphDrawer;
import edu.metagenomenewtest.presenter.GraphRepresentation;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class PairwiseComparisonGraphGroup extends GraphGroup {
    private double maxLogAbundancy;
    public PairwiseComparisonGraphGroup(Graph key) {
        super(key);
        maxLogAbundancy = maxLogAbundancy();
    }

    //TODO there should be an option to manually cap max and min displayed log abundancy
    private double maxLogAbundancy(){
        List<Double> logAbundancies = new ArrayList<>();
        for (Node v : this.graphKey.nodes()) {
            for (int i = 0; i < this.getNumSamples(); i++) {
                for (int j = 0; j < i ; j++) {
                    Double logAbundancy = ((ComparativeTreeNode) v).getLogAbundancies(i, j, true);
                    if (logAbundancy != null)
                        logAbundancies.add(logAbundancy);
                }
            }
        }
        //TODO change orelse
        double min = logAbundancies.stream().mapToDouble(d -> d).min().orElse(Math.log((double) 1 / 100000));
        double max = logAbundancies.stream().mapToDouble(d -> d).max().orElse(Math.log(100000));
        return Math.max(Math.abs(min), Math.abs(max));
    }

    @Override
    public SaturationColorScale createColorScale(Color minColor, Color maxColor) {
        this.colorScale = new SaturationColorScale(maxLogAbundancy, minColor, maxColor);
        return (SaturationColorScale) this.colorScale;
    }

    public GraphRepresentation representationWithLogColorsBetween(int sampleId1, int sampleId2){
        return GraphDrawer.drawGraph(this.graphKey, layout,
                comparativeTreeNode -> comparativeTreeNode.getLogAbundancies(sampleId1, sampleId2,
                        true),
                colorScale, 10, false);
    }




}
