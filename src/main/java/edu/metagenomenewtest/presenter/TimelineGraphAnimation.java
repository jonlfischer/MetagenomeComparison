package edu.metagenomenewtest.presenter;

import jloda.graph.Graph;
import edu.metagenomenewtest.model.graphGroup.TimelineGraphGroup;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;

public class TimelineGraphAnimation {
    /**
     * create a JavaFX timeline from a graphGroup, meaning a graph induced from multiple samples / files with
     * ComparativeTreeNodes. Each KeyFrame represents one sample/file. In the timeline, the color of nodes and edges
     * changes according to __ right now the relative abundancy __
     * @param graphGroup the graphGroup to animate
     * @param stepTimeMs the time of transition from one sample to the next
     * @return a timeline with animated colors
     */
    public static Timeline createTimeline(TimelineGraphGroup graphGroup, int stepTimeMs){
        int time = 0;
        int step = 0;
        Graph graph = graphGroup.getGraphKey();
        Timeline timeline = new Timeline();
        for (int i = 0; i < graphGroup.getNumSamples(); i++){
            ArrayList<KeyValue> keyValues = graphGroup.getColorKeyValuesAtPosition(i);
            KeyFrame frame = new KeyFrame(Duration.millis(time),
                    graphGroup.getSampleNameFromID(i),
                    null,
                    keyValues);
            timeline.getKeyFrames().add(frame);
            time += stepTimeMs;
            step ++;
        }
        return timeline;
    }
}
