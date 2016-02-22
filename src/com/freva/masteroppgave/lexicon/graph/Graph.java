package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.lexicon.utils.*;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.util.*;

public class Graph implements Progressable {
    private static final float edgeThreshold = 0.3f;
    private static final int pathLength = 3;
    private static final int neighborLimit = 30;

    private HashMap<String, Node> nodes = new HashMap<>();
    private PriorPolarityLexicon priorPolarityLexicon;

    private int currentProgress = 0;
    private int totalProgress = 0;


    public void updatePhraseContext(String token1, String token2, int scoreLeft, int scoreRight) {
        if(! nodes.containsKey(token1)) {
            nodes.put(token1, new Node());
        }

        if(! nodes.containsKey(token2)) {
            nodes.put(token2, new Node());
        }

        nodes.get(token1).updatePhraseContext(token2, scoreLeft, scoreRight);
        nodes.get(token2).updatePhraseContext(token1, scoreRight, scoreLeft);
    }


    /**
     * Initializes a HashMap containing all prior polarity words existing in the graph.
     * @param priorPolarityLexicon - A HashMap containing the polarity words in the graph. Ex.:{"good" : 3, "bad" : -3, ...}
     */
    public void setPriorPolarityLexicon(PriorPolarityLexicon priorPolarityLexicon) {
        this.priorPolarityLexicon = priorPolarityLexicon;
    }

    /**
     * Comparing each node, checking if there should be an edge between them
     */
    public void createAndWeighEdges() {
        List<String> nodeList = new ArrayList<>(nodes.keySet());
        totalProgress = nodeList.size() * (nodeList.size()-1) / 2;
        currentProgress = 0;
        int[][] coOccurrences = new int[nodes.size()][nodes.size()*2];
        for (int i = 0; i < nodeList.size(); i++) {
            String key1 = nodeList.get(i);
            for (int j = 0; j < nodeList.size(); j++ ) {
                String key2 = nodeList.get(j);
                coOccurrences[i][j*2] = nodes.get(key1).getLeftScoreForWord(key2);
                coOccurrences[i][j*2+1] = nodes.get(key1).getRightScoreForWord(key2);
            }
        }
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(coOccurrences, nodeList);
        createEdges(pearsonsCorrelation, nodeList);
        currentProgress++;
    }

    /**
     * Compares a pair of nodes and adds them as neighbors if their similarity is larger than set edgeThreshold
     * @param node1 - first node to be compared
     * @param node2 - second node to be compared
     */
    private void createEdges(PearsonsCorrelation pearsonsCorrelation, List<String> phrases) {
        for(int i = 0; i < phrases.size(); i++) {
            for(int j = i + 1; j < phrases.size(); j++) {
                double leftSimilarity = pearsonsCorrelation.getLeftSimilarityBetween(phrases.get(i), phrases.get(j));
                double rightSimilarity = pearsonsCorrelation.getRightSimilarityBetween(phrases.get(i), phrases.get(j));
                Node node1 = nodes.get(phrases.get(i)), node2 = nodes.get(phrases.get(j));
                double similarity = Math.max(leftSimilarity, rightSimilarity);
                if(similarity >= edgeThreshold) {
                    node1.addNeighbor(new Edge(node2, similarity));
                    node2.addNeighbor(new Edge(node1, similarity));
                }
            }
        }
    }



    /**
     * Propagates sentiment scores from nodes with prior polarity values to nodes on a path with length = pathLength from the node.
     * The prior polarity nodes propagates its sentiment score to its neighbors, the neighbors then propagates to their neighbors and so on until the pathLength is reached.
     * Each node only propagates sentiment to its x = neighborLimit highest weighted neighbors.
     */
    public void propagateSentiment() {
        currentProgress = 0;
        totalProgress = nodes.size();
        for(Map.Entry<String, Node> entry: nodes.entrySet()) {
            if(priorPolarityLexicon.hasWord(entry.getKey())) {
                ArrayList<Node> nodesToCheck = new ArrayList<>();
                nodesToCheck.add(entry.getValue());
                entry.getValue().updateSentimentScore((double) priorPolarityLexicon.getPolarity(entry.getKey()), entry.getValue());
                for(int i = 0; i < pathLength; i++) {
                    ArrayList<Node> nodesToCheckNext = new ArrayList<>();
                    for(Node nodeToCheck : nodesToCheck) {
                        ArrayList<Edge> neighbors = nodeToCheck.getNeighbors();
                        Collections.sort(neighbors);
                        int counter = 0;
                        for (Edge edge : neighbors) {
                            if(counter >= neighborLimit) break;
                            edge.getNeighbor().updateSentimentScore(nodeToCheck.getCurrentScore()*edge.getWeight(), nodeToCheck);
                            nodesToCheckNext.add(edge.getNeighbor());
                            counter++;
                        }
                    }
                    nodesToCheck = nodesToCheckNext;
                }
            }
            currentProgress++;
        }

        //Calculate Beta
        //Normalize scores
    }


    public Map<String, Double> getLexicon() {
        Map<String, Double> lexicon = new HashMap<>();
        for(Map.Entry<String, Node> entry: nodes.entrySet()) {
            lexicon.put(entry.getKey(), entry.getValue().getSentimentScore());
        }

        return lexicon;
    }

    @Override
    public double getProgress() {
        return (totalProgress == 0 ? 0 : 100.0*currentProgress/totalProgress);
    }
}
