package edu.metagenomecomparison.model;

import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeIntArray;
import javafx.geometry.Point2D;
import jloda.graph.Graph;

public class GraphLayout {
    private Graph graph;

    private Node[] nodes;

    private int[][] edges;

    private double[][] coordinates;


    public GraphLayout(Graph graph, Node[] nodes, int[][] edges, double[][] coordinates) {
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
        this.coordinates = coordinates;
    }

    public static GraphLayout fromNodeArray(Graph graph, NodeArray<Point2D> nodePoints){
        double[][] coordinates = new double[2][graph.getMaxNodeId()];
        int[][] edges = new int[2][graph.getMaxEdgeId()];
        Node[] nodes = new Node[graph.getMaxNodeId()];
        NodeIntArray node2id = graph.newNodeIntArray();
        for (int v = 0; v < nodes.length; v++) {
            node2id.put(nodes[v], v);
        }
        int eId = 0;
        for (var e : graph.edges()) {
            edges[0][eId] = node2id.get(e.getSource());
            edges[1][eId] = node2id.get(e.getTarget());
            eId++;
        }


        if (graph.getNumberOfNodes() > 0) {
            if (nodes != null) {
                for (var v : graph.nodes()) {
                    final int id = node2id.get(v);
                    if (nodePoints != null) {
                        coordinates[0][id] = nodePoints.get(v).getX();
                        coordinates[1][id] = nodePoints.get(v).getY();
                    } else {
                        double width = Math.sqrt(graph.getNumberOfNodes()) * 10;
                        for (int i = 0; i < graph.getNumberOfNodes(); i++) {
                            coordinates[0][i] = i == 0 ? 0 : (Math.random() * width - width / 2);
                            coordinates[1][i] = i == 0 ? 0 : (Math.random() * width - width / 2);
                        }
                    }
                }
            }
        }
        return new GraphLayout(graph, nodes, edges, coordinates);
    }

    public Graph getGraph() {
        return graph;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public int[][] getEdges() {
        return edges;
    }

    public double[][] getCoordinates() {
        return coordinates;
    }
}
