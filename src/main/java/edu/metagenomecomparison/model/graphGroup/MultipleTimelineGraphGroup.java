package edu.metagenomecomparison.model.graphGroup;

import jloda.graph.Graph;
import javafx.animation.KeyValue;

import java.util.ArrayList;

public class MultipleTimelineGraphGroup extends TimelineGraphGroup{
    int numTimelines;
    public MultipleTimelineGraphGroup(Graph updatedKey) {
        super(updatedKey);
        numTimelines = 2;
    }

    public void addTimeline(Graph updatedKey, int numTimeSteps) throws Exception {
        if (numTimeSteps != this.numTimeSteps)
            throw new Exception("cannot construct multiple timeline graph when number of time steps is unequal");
        this.graphKey = updatedKey;
        numTimelines++;
    }

    public ArrayList<KeyValue> getColorKeyValuesAtPosition(int sampleID, int timeLineNo){
        return getColorKeyValuesAtPosition(sampleID + timeLineNo * this.numTimeSteps);
    }
}
