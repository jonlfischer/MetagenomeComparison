package edu.metagenomenewtest.model.graphGroup;

import edu.metagenomenewtest.model.ComparativeTreeNode;
import jloda.graph.Graph;
import jloda.graph.Node;
import edu.metagenomenewtest.presenter.Coloring.GradientColorScale;
import javafx.animation.KeyValue;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class TimelineGraphGroup extends GraphGroup {
    private GradientColorScale colorScale;
    int numTimeSteps;
    public TimelineGraphGroup(Graph key) {
        super(key);
        this.numTimeSteps = ((ComparativeTreeNode) key.getFirstNode()).getNumSamples();
    }


    public GradientColorScale createColorScale(Color MINCOLOR, Color MAXCOLOR){
        this.colorScale = new GradientColorScale(0, 1, MINCOLOR, MAXCOLOR, Color.TRANSPARENT);
        return this.colorScale;
    }

    public ArrayList<KeyValue> getColorKeyValuesAtPosition(int sampleID){
        ArrayList<KeyValue> keyValues = new ArrayList<>();
        for (Node v : graphKey.nodes()){
            ComparativeTreeNode node = (ComparativeTreeNode) v;
            KeyValue keyValue = new KeyValue(((ComparativeTreeNode) v).getCircle().fillProperty(),
                    colorScale.getValueColor(node.getRelativeSummed(sampleID)));
            keyValues.add(keyValue);
        }
        return keyValues;
    }
}
