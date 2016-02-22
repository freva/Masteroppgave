package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Node implements Comparable<Node> {
    private static final int phraseVectorSize = 10;

    private HashMap<String, Integer> rightSideContextWords = new HashMap<>();
    private HashMap<String, Integer> leftSideContextWords = new HashMap<>();
    private HashMap<Node, Double> posValues = new HashMap<>();
    private HashMap<Node, Double> negValues = new HashMap<>();
    private ArrayList<Edge> neighbors = new ArrayList<>();

    private ContextWords contextWordsCache;
    private boolean cacheUpToDate = false;

    private double currentScore;
    private String phrase;

    public Node(String phrase) {
        this.phrase = phrase;
    }

    public void updatePhraseContext(String context, int scoreLeft, int scoreRight) {
        MapUtils.incrementMapByValue(rightSideContextWords, context, scoreLeft);
        MapUtils.incrementMapByValue(leftSideContextWords, context, scoreRight);

        cacheUpToDate = false;
    }


//    public ContextWords getContextWords() {
//        if(! cacheUpToDate) {
//            Map<String, Double> leftSideContext = getFrequentContextWords(leftSideContextWords);
//            Map<String, Double> rightSideContext = getFrequentContextWords(rightSideContextWords);
//            contextWordsCache = new ContextWords(leftSideContext, rightSideContext);
//            cacheUpToDate = true;
//        }
//
//        return contextWordsCache;
//    }


    private Map<String, Double> getFrequentContextWords(Map<String, Integer> map) {
        Map<String, Double>  normalizedMap = MapUtils.normalizeMapBetween(map, 0, 1);
        return MapUtils.getNLargest(normalizedMap, phraseVectorSize);
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
    public ArrayList<Edge> getNeighbors() {
        return neighbors;
    }


    /**
     * Adds a neighbor node to the array of neighbors
     * @param neighbor node
     */
    public void addNeighbor(Edge neighbor) {
        neighbors.add(neighbor);
        posValues.put(neighbor.getNeighbor(), 0.0);
    }


    /**
     * Updates the current propagation score, as well as either the max positive score or the min negative score
     * propagated from the given neighbor node.
     * @param score - The score propagated from the given neighbor.(Can be positive or negative)
     * @param neighbor - The neighbor node propagating the sentiment score to the current node.
     */
    public void updateSentimentScore(double score, Node neighbor) {
        currentScore = score;
        if(score < 0) {
            updateNegScore(score, neighbor);
        } else {
            updatePosScore(score, neighbor);
        }
    }


    /**
     * Updates the max positive sentiment score propagated from the given neighbor if the given score is bigger than
     * the previous max score propagated from the neighbor.
     * @param score - The positive sentiment score propagated from the neighbor.
     * @param neighbor - The neighbor node propagating the sentiment score.
     */
    private void updatePosScore(double score, Node neighbor) {
        if(!posValues.containsKey(neighbor)) {
            posValues.put(neighbor, score);
        } else {
            double maxScore = Math.max(posValues.get(neighbor), score);
            posValues.put(neighbor, maxScore);
        }
    }


    /**
     * Updates the min negative sentiment score propagated from the given neighbor if the given score is smaller than
     * the previous min score propagated from the neighbor.
     * @param score - The negative sentiment score propagated from the neighbor.
     * @param neighbor - The neighbor node propagating the sentiment score.
     */
    private void updateNegScore(double score, Node neighbor) {
        if(!negValues.containsKey(neighbor)) {
            negValues.put(neighbor, score);
        } else {
            double minScore = Math.min(negValues.get(neighbor), score);
            negValues.put(neighbor, minScore);
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
     * Sums up the sentiment scores contained in a HashMap. This method is called once for the HashMaps containing
     * positive and negative sentiment scores respectively.
     * @param scores - HashMap containing sentiment scores
     * @return - The sum of the sentiment scores.
     */
    private double sumScores(HashMap<Node, Double> scores) {
        double total = 0.0;
        for(Double score : scores.values()) {
            total += score;
        }
        return total;
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
