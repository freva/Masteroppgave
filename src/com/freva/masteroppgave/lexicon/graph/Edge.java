package com.freva.masteroppgave.lexicon.graph;

public class Edge implements Comparable<Edge> {
    private Node neighbor;
    private double weight;

    /**
     * Edge constructor holding the edge weight and the node the edge is connected to.
     * @param neighbor - The node connected to the current node via this edge. The neighbor.
     * @param weight - The weight of the edge. Represents the Cosine Similarity between current node and the neighbor node.
     */
    public Edge(Node neighbor, double weight) {
        this.neighbor = neighbor;
        this.weight = weight;
    }

    /**
     * Returns the neighbor node.
     * @return The neighbor node.
     */
    public Node getNeighbor() {
        return neighbor;
    }

    /**
     * Returns the edge weight
     * @return The edge weight
     */
    public double getWeight() {
        return weight;
    }

    @Override
    public int compareTo(Edge o) {
        return (int) Math.signum(weight - o.weight);
    }
}
