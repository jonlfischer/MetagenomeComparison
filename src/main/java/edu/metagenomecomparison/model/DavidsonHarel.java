package edu.metagenomecomparison.model;

import jloda.graph.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.ProgressBar;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 */
public class DavidsonHarel {
    /**
     *
     */
    private final Graph graph;
    private final Node[] nodes;
    private final double[][] coordinates;
    private final int[][] edges;

    private final NodeIntArray node2id;

    public HashSet<Integer> movedNodes = new HashSet<>();


    public DavidsonHarel(Graph graph, NodeArray<Point2D> nodePoints) {
        this.graph = graph;
        this.coordinates = new double[2][graph.getMaxNodeId()];
        node2id = graph.newNodeIntArray();
        edges = new int[2][graph.getMaxEdgeId()];
        nodes = new Node[graph.getMaxNodeId()];
        {
            int i = 0;
            for (var v : graph.nodes()) {
                nodes[i] = v;
                i++;
            }
        }
        initialize(nodePoints);
    }



    //null if nodePoints should get randomly generated
    public void initialize(NodeArray<Point2D> nodePoints) {
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
                            this.coordinates[0][i] = i == 0 ? 0 : (Math.random() * width - width / 2);
                            this.coordinates[1][i] = i == 0 ? 0 : (Math.random() * width - width / 2);
                        }
                    }
                }
            }
        }
    }

    // not included: fix root at center, (find/define root)
    public void layoutWithDH(double coolingFactor,
                             int maxIterations, int fineIterations) {
        int noNodes = graph.getNumberOfNodes();
        int noEdges = graph.getNumberOfEdges();
        System.out.println("nodes" + noNodes);
        System.out.println("edges" + noEdges);

        //weights are used here like in the example that we are trying to reproduce
        //commented out weights are as suggested by igraph code
        double GRAPH_DENSITY = (double) (2 * graph.getNumberOfEdges()) / noNodes * (noNodes - 1);
        double WEIGHT_NODE_DIST = 13; //1.0F; //13
        double WEIGHT_BORDER = 0.2f;
        double WEIGHT_EDGE_LENGTHS = 0.5; //GRAPH_DENSITY / 10; //0.5
        double WEIGHT_EDGE_CROSS = 100;//1 - Math.sqrt(GRAPH_DENSITY); //100
        double WEIGHT_NODE_EDGE_DIST = 1; // (1 - GRAPH_DENSITY) / 5; //1

        //double width = (double) (Math.sqrt(noNodes) * 10);
        double minX = Arrays.stream(coordinates[0]).min().getAsDouble();
        double maxX = Arrays.stream(coordinates[0]).max().getAsDouble();
        double minY = Arrays.stream(coordinates[1]).min().getAsDouble();
        double maxY = Arrays.stream(coordinates[1]).max().getAsDouble();

        double absMax = Math.max(Math.max(maxX, maxY),
                Math.max(Math.abs(minX), Math.abs(minY)));

        int noTries = 30;
        double fineTuningFactor = 0.01f;
        double[] tryX = new double[noTries];
        double[] tryY = new double[noTries];
        int[] try_idx = IntStream.range(0, noTries).toArray();
        //later shuffle for random order of moved nodes
        int[] perm = IntStream.range(0, noNodes).toArray();

        double width = Math.sqrt(noNodes) * 10;

        for (int i = 0; i < noNodes; i++) {
            coordinates[0][i] = coordinates[0][i] / absMax * 0.5 * width;
            coordinates[1][i] = coordinates[1][i] / absMax * 0.5 * width;
        }


        //double width = (double) (Math.min(maxX-minX, maxY-minY));
        double height = width;
        double moveRadius = width / 2;
        System.out.println(width);

        for (int i = 0; i < noTries; i++) {
            double phi = 2 * Math.PI / noTries * i;
            tryX[i] = Math.cos(phi);
            tryY[i] = Math.sin(phi);
        }

        for (int round = 0; round < maxIterations + fineIterations; round++) {
            perm = shuffle(perm);

            boolean isFineTuning = round >= maxIterations;
            if (isFineTuning) {
                double fx = fineTuningFactor * (maxX - minX);
                double fy = fineTuningFactor * (maxY - minY);
                moveRadius = Math.min(fx, fy);
            }

            //this is where the loop with moving nodes and energy calculation starts
            for (int p = 0; p < noNodes; p++) {
                int v = perm[p];
                Node vNode = nodes[v];

                try_idx = shuffle(try_idx);

                for (int t = 0; t < noTries; t++) {
                    double diffEnergy = 0.0f;
                    int ti = try_idx[t];

                    /* Try moving it */
                    double oldX = coordinates[0][v];
                    double oldY = coordinates[1][v];
                    double newX = oldX + moveRadius * tryX[ti];
                    double newY = oldY + moveRadius * tryY[ti];


                    if (newX < -width / 2) newX = -width / 2 + 1e-6;
                    if (newX > width / 2) newX = width / 2 - 1e-6;
                    if (newY < -height / 2) newY = -height / 2 + 1e-6;
                    if (newY > height / 2) newY = height / 2 - 1e-6;

                    //these are always true for hardcoded weights, still included for knowing which part is which
                    if (WEIGHT_NODE_DIST != 0) {
                        for (int u = 0; u < noNodes; u++) {
                            if (u == v) continue;
                            double odx = oldX - coordinates[0][u];
                            double ody = oldY - coordinates[1][u];
                            double dx = newX - coordinates[0][u];
                            double dy = newY - coordinates[1][u];
                            double odist2 = odx * odx + ody * ody;
                            double dist2 = dx * dx + dy * dy;
                            diffEnergy += WEIGHT_NODE_DIST / dist2 - WEIGHT_NODE_DIST / odist2;
                        }
                    }

                    if (WEIGHT_BORDER != 0) {
                        double odx1 = width / 2 - oldX, odx2 = oldX + width / 2;
                        double ody1 = height / 2 - oldY, ody2 = oldY + height / 2;
                        double dx1 = width / 2 - newX, dx2 = newX + width / 2;
                        double dy1 = height / 2 - newY, dy2 = newY + height / 2;
                        odx1 = odx1 < 0 ? 2 : odx1;
                        odx2 = odx2 < 0 ? 2 : odx2;
                        ody1 = ody1 < 0 ? 2 : ody1;
                        ody2 = ody2 < 0 ? 2 : ody2;
                        diffEnergy -= WEIGHT_BORDER *
                                (1.0 / (odx1 * odx1) + 1.0 / (odx2 * odx2) +
                                        1.0 / (ody1 * ody1) + 1.0 / (ody2 * ody2));
                        diffEnergy += WEIGHT_BORDER *
                                (1.0 / (dx1 * dx1) + 1.0 / (dx2 * dx2) +
                                        1.0 / (dy1 * dy1) + 1.0 / (dy2 * dy2));

                    }

                    if (WEIGHT_EDGE_LENGTHS != 0) {
                        for (Node neigh : vNode.adjacentNodes()) {
                            int u = node2id.get(neigh);
                            double odx = oldX - coordinates[0][u];
                            double ody = oldY - coordinates[1][u];
                            double odist2 = odx * odx + ody * ody;
                            double dx = newX - coordinates[0][u];
                            double dy = newY - coordinates[1][u];
                            double dist2 = dx * dx + dy * dy;
                            diffEnergy += WEIGHT_EDGE_LENGTHS * (dist2 - odist2);
                        }
                    }

                    if (WEIGHT_EDGE_CROSS != 0) {
                        int no = 0;
                        for (Node neigh : vNode.adjacentNodes()) {
                            int u = node2id.get(neigh);
                            double uX = coordinates[0][u];
                            double uY = coordinates[1][u];
                            for (int e = 0; e < noEdges; e++) {
                                int u1 = edges[0][e];
                                int u2 = edges[1][e];
                                if (u1 == v || u2 == v || u1 == u || u2 == u) continue;
                                double u1X = coordinates[0][u1];
                                double u1Y = coordinates[1][u1];
                                double u2X = coordinates[0][u2];
                                double u2Y = coordinates[1][u2];
                                boolean isIntersectOld = isSegmentIntersect(oldX, oldY, uX, uY, u1X, u1Y, u2X, u2Y);
                                boolean isIntersectNew = isSegmentIntersect(newX, newY, uX, uY, u1X, u1Y, u2X, u2Y);
                                no -= isIntersectOld ? 1 : 0;
                                no += isIntersectNew ? 1 : 0;


                            }
                        }
                        diffEnergy += WEIGHT_EDGE_CROSS * no;
                    }

                    if (WEIGHT_NODE_EDGE_DIST != 0 && isFineTuning) {
                        /* edges not incident from v*/
                        for (int e = 0; e < noEdges; e++) {
                            int u1 = edges[0][e];
                            int u2 = edges[1][e];
                            if (u1 == v || u2 == v) continue;
                            double u1X = coordinates[0][u1];
                            double u1Y = coordinates[1][u1];
                            double u2X = coordinates[0][u2];
                            double u2Y = coordinates[1][u2];
                            double dEV = pointLineDistanceSq(oldX, oldY, u1X, u1Y, u2X, u2Y);
                            diffEnergy -= WEIGHT_NODE_EDGE_DIST / dEV;
                            dEV = pointLineDistanceSq(newX, newY, u1X, u1Y, u2X, u2Y);
                            diffEnergy += WEIGHT_NODE_EDGE_DIST / dEV;
                        }

                        /* other nodes from vs incident edges*/
                        Iterable<Edge> incident = nodes[v].adjacentEdges();
                        for (Edge myEdge : incident) {
                            int u = node2id.get(myEdge.getOpposite(vNode));
                            double uX = coordinates[0][u];
                            double uY = coordinates[1][u];
                            for (int w = 0; w < noNodes; w++) {
                                if (w == v || w == u) continue;
                                double wX = coordinates[0][w];
                                double wY = coordinates[1][w];
                                double dEV = pointLineDistanceSq(wX, wY, oldX, oldY, uX, uY);
                                diffEnergy -= WEIGHT_NODE_EDGE_DIST / dEV;
                                dEV = pointLineDistanceSq(wX, wY, newX, newY, uX, uY);
                                diffEnergy += WEIGHT_NODE_EDGE_DIST / dEV;
                            }
                        }
                    }

                    if ((diffEnergy < 0) ||
                            (!isFineTuning && Math.random() < Math.exp(-diffEnergy / moveRadius))) {
                        coordinates[0][v] = newX;
                        coordinates[1][v] = newY;
                        movedNodes.add(v);
                        if (newX < minX) {
                            minX = newX;
                        } else if (newX > maxX) {
                            maxX = newX;
                        }
                        if (newY < minY) {
                            minY = newY;
                        } else if (newY > maxY) {
                            maxY = newY;
                        }
                    }
                }
            }
            moveRadius *= coolingFactor;
        }
    }

    public GraphLayout getLayout(){
        return new GraphLayout(this.graph, this.nodes, this.edges, this.coordinates);
    }
    public double[][] getCoordinates() {
        return this.coordinates;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public int[][] getEdges() {
        return edges;
    }

    /**
     * shuffle an array
     *
     * @param array the array to shuffle
     * @return the shuffled array
     */
    private int[] shuffle(int[] array) {
        List<Integer> list = new java.util.ArrayList<Integer>(Arrays.stream(array).boxed().toList());
        Collections.shuffle(list);
        return list.stream().mapToInt(i -> i).toArray();
    }

    /**
     * calculate distance from line to point following iGraph example
     *
     * @param vX  point X
     * @param vY  point Y
     * @param u1X line endpoint 1 X
     * @param u1Y line endpoint 1 Y
     * @param u2X line endpoint 2 X
     * @param u2Y line endpoint 2 Y
     * @return shortest distance from point to line ** 2
     */
    private double pointLineDistanceSq(double vX, double vY,
                                       double u1X, double u1Y,
                                       double u2X, double u2Y) {
        double dx = u2X - u1X;
        double dy = u2Y - u1Y;
        double l2 = dx * dx + dy * dy;
        double t, p_x, p_y;
        if (l2 == 0) {
            return (vX - u1X) * (vX - u1X) + (vY - u1Y) * (vY - u1Y);
        }
        t = ((vX - u1X) * dx + (vY - u1Y) * dy) / l2;
        if (t < 0.0) {
            return (vX - u1X) * (vX - u1X) + (vX - u1Y) * (vY - u1Y);
        } else if (t > 1.0) {
            return (vX - u2X) * (vX - u2X) + (vY - u2Y) * (vY - u2Y);
        }
        p_x = u1X + t * dx;
        p_y = u1Y + t * dy;
        return (vX - p_x) * (vX - p_x) + (vY - p_y) * (vY - p_y);
    }

    private boolean isSegmentIntersect(double p0X, double p0Y,
                                       double p1X, double p1Y,
                                       double p2X, double p2Y,
                                       double p3X, double p3Y) {
        double s1X = p1X - p0X;
        double s1Y = p1Y - p0Y;
        double s2X = p3X - p2X;
        double s2Y = p3Y - p2Y;

        double s1, s2, t1, t2, s, t;
        s1 = (-s1Y * (p0X - p2X) + s1X * (p0Y - p2Y));
        s2 = (-s2X * s1Y + s1X * s2Y);
        if (s2 == 0) {
            return false;
        }
        t1 = (s2X * (p0X - p2Y) - s2Y * (p0Y - p2X));
        t2 = (-s2X * s1Y + s1X * s2Y);
        s = s1 / s2;
        t = t1 / t2;

        return (s >= 0 && s <= 1 && t >= 0 && t <= 1);
    }

    //TODO datermine if this is the way or reimplement java code, wrap in process
    public void davidsonHarelWithPythonIgraph() throws IOException {
        StringBuilder edgeBuilder = new StringBuilder("[");
        for (int i = 0; i < edges[0].length; i ++) {
            edgeBuilder.append("(").append(String.valueOf(edges[0][i])).append(",");
            edgeBuilder.append(String.valueOf(edges[1][i])).append(")");
            if (i != edges[0].length - 1)
                edgeBuilder.append(",");
        }
        edgeBuilder.append("]");
        FileWriter writer = new FileWriter("/home/jo/Uni/Thesis/code/python/graph.txt");
        writer.write(edgeBuilder.toString());
        writer.close();

        //coordinate builder
        StringBuilder coordBuilder = new StringBuilder("[");
        for (int i = 0; i < coordinates[0].length; i ++) {
            coordBuilder.append("[").append(String.valueOf(coordinates[0][i])).append(", ");
            coordBuilder.append(String.valueOf(coordinates[1][i])).append("]");
            if (i != coordinates[0].length - 1)
                coordBuilder.append(",");

        }
        coordBuilder.append("]");
        FileWriter coordWriter = new FileWriter("/home/jo/Uni/Thesis/code/python/coords.txt");
        coordWriter.write(coordBuilder.toString());
        coordWriter.close();

        //String[] conda = new String[]{"bash", "/home/jo/miniconda3/bin/conda", "activate", "base"};
        String[] python = new String[]{"/home/jo/miniconda3/bin/python", "/home/jo/Uni/Thesis/code/python/dhlayout.py",
                "/home/jo/Uni/Thesis/code/python/graph.txt", "/home/jo/Uni/Thesis/code/python/coords.txt"};
        Process proc = Runtime.getRuntime().exec(python);
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String line = br.readLine();

        while (line != null) {
            System.out.println(br.readLine());
            line = br.readLine();
        }
        proc.destroy();

        BufferedReader coordReader = new BufferedReader(new FileReader("/home/jo/Uni/Thesis/code/python/coords.txt"));
        String coordinates = coordReader.readLine();
        coordinates = coordinates.substring(2, coordinates.length() - 2);
        String[] splitCoords = coordinates.split("], \\[");
        for (int i = 0; i < splitCoords.length; i ++){
            String[] point = splitCoords[i].split(", ");
            this.coordinates[0][i] = Double.parseDouble(point[0]);
            this.coordinates[1][i] = Double.parseDouble(point[1]);
        }
    }

    public Service<GraphLayout> davidsonHarelService(){
        return new Service<GraphLayout>() {
            @Override
            protected Task<GraphLayout> createTask() {
                return new Task<GraphLayout>() {
                    @Override
                    protected GraphLayout call() throws Exception {
                        updateValue(getLayout());
                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, 100);
                        updateMessage("Running Davidson-Harel Layout algorithm");
                        davidsonHarelWithPythonIgraph();
                        updateProgress(100, 100);
                        updateProgress(0, 100);
                        updateMessage("");
                        GraphLayout graphLayout = getLayout();
                        updateValue(graphLayout);
                        return graphLayout;
                    }
                };
            }
        };
    }
}