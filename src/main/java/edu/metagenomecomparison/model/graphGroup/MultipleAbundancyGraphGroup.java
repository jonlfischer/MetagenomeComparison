package edu.metagenomecomparison.model.graphGroup;

import jloda.graph.Graph;
import edu.metagenomecomparison.presenter.Coloring.GradientColorScale;
import edu.metagenomecomparison.presenter.GraphDrawer;
import edu.metagenomecomparison.presenter.GraphRepresentation;
import javafx.scene.paint.Color;

public class MultipleAbundancyGraphGroup extends GraphGroup{
    private GradientColorScale colorScale;
    public MultipleAbundancyGraphGroup(Graph key) {
        super(key);
    }

    @Override
    public GradientColorScale createColorScale(Color MINCOLOR, Color MAXCOLOR){
        this.colorScale = new GradientColorScale(0, 1, MINCOLOR, MAXCOLOR, Color.TRANSPARENT);
        return this.colorScale;
    }

    public GraphRepresentation representationOfSampleId(int sampleId){
        return GraphDrawer.drawGraph(this.graphKey, layout,
                comparativeTreeNode -> comparativeTreeNode.getRelativeSummed(sampleId),
                colorScale, 10, false);
    }
}
