package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
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
    public double getSentimentScore(double beta) {
        return getPositiveSentimentScore() - beta*getNegativeSentimentScore();
    }


    public double getPositiveSentimentScore() {
        if(posValues.values().size() == 0) return 0;
        return posValues.values().stream().mapToDouble(i-> i).sum();
    }

    public double getNegativeSentimentScore() {
        if(negValues.values().size() == 0) return 0;
        return negValues.values().stream().mapToDouble(i-> i).sum();
    }


    /**
     * Returns the current propagation score. The score that is going to be propagated out from the node to its neighbors.
     * @return - Current propagation score.
     */
    public double getCurrentScore(){
        return currentScore;
    }


    public String getPhrase() {
        return phrase;
    }
}
