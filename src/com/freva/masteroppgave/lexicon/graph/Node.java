package com.freva.masteroppgave.lexicon.graph;

import java.util.ArrayList;
import java.util.HashMap;

public class Node implements Comparable<Node> {
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

    /**
     * Updates the current propagation score, as well as either the max positive score or the min negative score propagated from the given neighbor node.
     * @param score - The score propagated from the given neighbor.(Can be positive or negative)
     * @param neighbor - The neighbor node propagating the sentiment score to the current node.
     */
    public void updateSentimentScore(double score, Node neighbor) {
        currentScore = score;
        if(score < 0) {
            updateNegScore(score, neighbor);
        }
        else updatePosScore(score, neighbor);
    }

    /**
     * Updates the max positive sentiment score propagated from the given neighbor if the given score is bigger than the previous max score propagated from the neighbor.
     * @param score - The positive sentiment score propagated from the neighbor.
     * @param neighbor - The neighbor node propagating the sentiment score.
     */
    private void updatePosScore(double score, Node neighbor) {
        if(!posValues.containsKey(neighbor.getPhrase())) {
            posValues.put(neighbor.getPhrase(), score);
        }
        else {
            double maxScore = Math.max(posValues.get(neighbor.getPhrase()), score);
            posValues.put(neighbor.getPhrase(), maxScore);
        }
    }

    /**
     * Updates the min negative sentiment score propagated from the given neighbor if the given score is smaller than the previous min score propagated from the neighbor.
     * @param score - The negative sentiment score propagated from the neighbor.
     * @param neighbor - The neighbor node propagating the sentiment score.
     */
    private void updateNegScore(double score, Node neighbor) {
        if(!negValues.containsKey(neighbor.getPhrase())) {
            negValues.put(neighbor.getPhrase(), score);
        }
        else {
            double minScore = Math.min(negValues.get(neighbor.getPhrase()), score);
            negValues.put(neighbor.getPhrase(), minScore);
        }
    }

    /**
     * Returns the overall sentiment score of the node.
     * @return - Sentiment score.
     */
    public double getSentimentScore() {
        return sumScores(posValues) + (sumScores(negValues));
    }

    /**
     * Returns the current propagation score. The score that is going to be propagated out from the node to its neighbors.
     * @return - Current propagation score.
     */
    public double getCurrentScore(){
        return currentScore;
    }

    /**
     * Sums up the sentiment scores contained in a HashMap. This method is called once for the HashMaps containing positive and negative sentiment scores respectively.
     * @param scores - HashMap containing sentiment scores
     * @return - The sum of the sentiment scores.
     */
    private double sumScores(HashMap<String, Double> scores) {
        double total = 0.0;
        for(Double score : scores.values()) {
            total += score;
        }
        return total;
    }

    @Override
    public int compareTo(Node other) {
        if(other == null) return -1;
        else return (int) Math.signum(other.getSentimentScore()-this.getSentimentScore());
    }
}
