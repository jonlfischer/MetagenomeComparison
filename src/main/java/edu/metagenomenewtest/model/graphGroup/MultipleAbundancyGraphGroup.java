package edu.metagenomenewtest.model.graphGroup;

import jloda.graph.Graph;
import edu.metagenomenewtest.presenter.Coloring.GradientColorScale;
import edu.metagenomenewtest.presenter.GraphDrawer;
import edu.metagenomenewtest.presenter.GraphRepresentation;
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
