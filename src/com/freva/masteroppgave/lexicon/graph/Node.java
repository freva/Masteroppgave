package com.freva.masteroppgave.lexicon.graph;

import java.util.ArrayList;

public class Node {
    private String phrase;
    private String[][] contextVector;
    private ArrayList<Edge> neighbors = new ArrayList<>();
    private double posScore = 0;
    private double negScore = 0;

    public Node(String phrase, String[][] contextVector) {
        this.phrase = phrase;
        this.contextVector = contextVector;
    }

    /**
     * Returns the phrase associated with the node.
     * @return phrase
     */
    public String getPhrase() {
        return phrase;
    }

    /**
     * Returns the context vector of the node-phrase.
     * @return context vector
     */
    public String[][] getContextVector() {
        return contextVector;
    }

    /**
     * Returns the nodes neighbors
     * @return neighbors
     */
    public ArrayList<Edge> getNeighbors() {
        return neighbors;
    }

    /**
     * Adds a neighbor node to the array of neighbors
     * @param neighbor node
     */
    public void addNeighbor(Edge neighbor) {
        neighbors.add(neighbor);
    }

    public void updatePosScore(double score) {
        posScore += score;
    }

    public void updateNegScore(double score) {
        negScore += score;
    }

    public double getSentimentScore() {
        return posScore - negScore;
    }
}
