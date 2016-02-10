package com.freva.masteroppgave.lexicon.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Node {
    private String phrase;
    private String[][] contextVector;
    private ArrayList<Edge> neighbors = new ArrayList<>();
    private HashMap<String, Double> posValues = new HashMap<>();
    private HashMap<String, Double> negValues = new HashMap<>();
    private double currentScore;

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
        posValues.put(neighbor.getNeighbor().getPhrase(), 0.0);
    }


    public void updateSentimentScore(double score, Node neighbor) {
        currentScore = score;
        if(score < 0) {
            updateNegScore(score, neighbor);
        }
        else updatePosScore(score, neighbor);
    }

    private void updatePosScore(double score, Node neighbor) {
        if(!posValues.containsKey(neighbor.getPhrase())) {
            posValues.put(neighbor.getPhrase(), score);
        }
        else {
            double maxScore = Math.max(posValues.get(neighbor.getPhrase()), score);
            posValues.put(neighbor.getPhrase(), maxScore);
        }
    }

    private void updateNegScore(double score, Node neighbor) {
        if(!negValues.containsKey(neighbor.getPhrase())) {
            negValues.put(neighbor.getPhrase(), score);
        }
        else {
            double minScore = Math.min(negValues.get(neighbor.getPhrase()), score);
            negValues.put(neighbor.getPhrase(), minScore);
        }
    }

    public double getSentimentScore() {
//        double beta = posValues.size()/negValues.size();
        return sumScores(posValues) + (sumScores(negValues));
    }

    public double getCurrentScore(){
        return currentScore;
    }

    private double sumScores(HashMap<String, Double> scores) {
        double total = 0.0;
        for(Double score : scores.values()) {
            total += score;
        }
        return total;
    }
}
