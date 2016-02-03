package com.freva.masteroppgave.graph;

import java.util.ArrayList;

public class Node {
    private String phrase;
    private String[] phraseVector;
    private ArrayList<Edge> neighbors = new ArrayList<>();
    private double posScore = 0;
    private double negScore = 0;

    public Node(String phrase, String[] phraseVector) {
        this.phrase = phrase;
        this.phraseVector = phraseVector;
    }

//    Returns the phrase associated with the Node.
    public String getPhrase() {
        return phrase;
    }

//    List of tweets containing the phrase/word.
    public String[] getPhraseVector() {
        return phraseVector;
    }

//    Returns array of all outgoing Edges or direct neighbors.
    public ArrayList<Edge> getNeighbors() {
        return neighbors;
    }

//    Adds Edge or neighbor to the array of Edges/neighbors.
    public void addNeighbor(Edge neighbor) {
        neighbors.add(neighbor);
    }

    public void updatePosScore(double score) {
        posScore += score;
    }

    public void udateNegScore(double score) {
        negScore += score;
    }

    public double getSentimentScore() {
        return posScore - negScore;
    }
}
