package edu.metagenomenewtest.model.graphGroup;

import edu.metagenomenewtest.model.ComparativeTreeNode;
import edu.metagenomenewtest.model.GraphLayout;
import jloda.graph.Graph;
import jloda.phylo.PhyloTree;
import edu.metagenomenewtest.presenter.Coloring.ColorScale;
import edu.metagenomenewtest.presenter.GraphDrawer;
import edu.metagenomenewtest.presenter.GraphRepresentation;
import javafx.scene.paint.Color;

import java.util.HashMap;

public abstract class GraphGroup {
    Graph graphKey;

    GraphLayout layout;

    private HashMap<String, Integer> sampleNameToID;

    private int maxAbundancy = 0;

    private int numSamples;

    ColorScale colorScale;



    public GraphGroup(Graph key) {
        this.graphKey = key;
        this.sampleNameToID = ((ComparativeTreeNode) key.getFirstNode()).getSampleNameToId();
        for (String sampleName : sampleNameToID.keySet()) {
            int treeMaxAbundancy = ((ComparativeTreeNode) ((PhyloTree) key).getRoot())
                    .getSummed(sampleNameToID.get(sampleName));
            if (treeMaxAbundancy > maxAbundancy)
                maxAbundancy = treeMaxAbundancy;
        }
        this.numSamples = ((ComparativeTreeNode) graphKey.getFirstNode()).getNumSamples();
    }



    public void setLayout(GraphLayout graphLayout){
        this.layout = graphLayout;
    }

    //TODO i dont know if this hack works every time
    public String getSampleNameFromID(int sampleID){
        return this.sampleNameToID.keySet().toArray(new String[sampleID + 1])[sampleID];
    }

    public abstract ColorScale createColorScale(Color minColor, Color maxColor);


    public GraphRepresentation drawMainGraph(int sampleID, double scalingFactor){
        return GraphDrawer.drawGraph(this.graphKey, this.layout,
                comparativeTreeNode -> (double) comparativeTreeNode.getSummed(sampleID),
                this.colorScale, scalingFactor, true);
    }

    //TODO at this point also only for relative summed abundancies



    public Graph getGraphKey() {
        return graphKey;
    }

    public GraphLayout getLayout() {
        return layout;
    }



    public HashMap<String, Integer> getSampleNameToID() {
        return sampleNameToID;
    }

    public int getMaxAbundancy() {
        return maxAbundancy;
    }

    public ColorScale getColorScale() {
        return colorScale;
    }

    public int getNumSamples() {
        return numSamples;
    }
//TODO something with relative abundancies in the trees or so, rather in ComparativeTreeNode
}
