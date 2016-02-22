package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node implements Comparable<Node> {
    private HashMap<String, Integer> rightSideContextWords = new HashMap<>();
    private HashMap<String, Integer> leftSideContextWords = new HashMap<>();
    private HashMap<Node, Double> posValues = new HashMap<>();
    private HashMap<Node, Double> negValues = new HashMap<>();
    private List<Edge> neighbors = new ArrayList<>();

    private double currentScore;
    private String phrase;

    public Node(String phrase) {
        this.phrase = phrase;
    }

    public void updatePhraseContext(String context, int scoreLeft, int scoreRight) {
        MapUtils.incrementMapByValue(rightSideContextWords, context, scoreLeft);
        MapUtils.incrementMapByValue(leftSideContextWords, context, scoreRight);
    }


    public int getRightScoreForWord(String word) {
        return rightSideContextWords.containsKey(word) ? rightSideContextWords.get(word) : 0;
    }

    public int getLeftScoreForWord(String word) {
        return leftSideContextWords.containsKey(word) ? leftSideContextWords.get(word) : 0;
    }

    /**
     * Returns the nodes neighbors
     * @return neighbors
     */
    public List<Edge> getNeighbors() {
        return neighbors;
    }


    /**
     * Adds a neighbor node to the array of neighbors
     * @param neighbor node
     */
    public void addNeighbor(Edge neighbor) {
        neighbors.add(neighbor);
    }


    /**
     * Updates the current propagation score, as well as either the max positive score or the min negative score
     * propagated from the given neighbor node.
     * @param neighbor - The neighbor node propagating the sentiment score to the current node.
     * @param score - The score propagated from the given neighbor.(Can be positive or negative)
     */
    public void updateSentimentScore(Node neighbor, double score) {
        currentScore = score;

        if(score > 0) {
            if(! posValues.containsKey(neighbor)) {
                posValues.put(neighbor, score);
            } else if(posValues.get(neighbor) < score) {
                posValues.put(neighbor, score);
            }
        } else {
            if(! negValues.containsKey(neighbor)) {
                negValues.put(neighbor, score);
            } else if(negValues.get(neighbor) > score) {
                negValues.put(neighbor, score);
            }
        }
    }


    /**
     * Returns the overall sentiment score of the node.
     * @return - Sentiment score.
     */
    public double getSentimentScore() {
        return sumScores(posValues) + sumScores(negValues);
    }


    /**
     * Returns the current propagation score. The score that is going to be propagated out from the node to its neighbors.
     * @return - Current propagation score.
     */
    public double getCurrentScore(){
        return currentScore;
    }


    /**
     * Sums up the sentiment scores contained in a HashMap. This method is called once for the HashMaps containing
     * positive and negative sentiment scores respectively.
     * @param scores - HashMap containing sentiment scores
     * @return - The sum of the sentiment scores.
     */
    private double sumScores(HashMap<Node, Double> scores) {
        return scores.values().parallelStream().reduce(0.0, Double::sum);
    }

    public String getPhrase() {
        return phrase;
    }

    @Override
    public int compareTo(Node other) {
        if(other == null) return -1;
        else return (int) Math.signum(other.getSentimentScore()-this.getSentimentScore());
    }
}
