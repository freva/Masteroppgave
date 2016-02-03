package com.freva.masteroppgave.graph;

public class Edge {

    private Node neighbor;
    private double weight;

    public Edge(Node neighbor, double weight) {
        this.neighbor = neighbor;
        this.weight = weight;
    }

    public Node getNeighbor() {
        return neighbor;
    }

    public double getWeight() {
        return weight;
    }
}
