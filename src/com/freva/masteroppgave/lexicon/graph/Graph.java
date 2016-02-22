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
            nodes.put(token1, new Node(token1));
        }

        if(! nodes.containsKey(token2)) {
            nodes.put(token2, new Node(token2));
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
    public int[][] getCoOccurrences() {
        List<Node> nodeList = new ArrayList<>(nodes.values());
        int[][] coOccurrences = new int[nodes.size()][nodes.size()*2];
        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = 0; j < nodeList.size(); j++ ) {
                coOccurrences[i][j*2] = nodeList.get(i).getLeftScoreForWord(nodeList.get(j).getPhrase());
                coOccurrences[i][j*2+1] = nodeList.get(j).getRightScoreForWord(nodeList.get(j).getPhrase());
            }
        }
        return coOccurrences;
    }


    public void createEdges(List<PairSimilarity<Node>> similarities) {
        for(PairSimilarity<Node> p : similarities) {
            if(p.getSimilarity() < edgeThreshold) continue;

            p.getEntry1().addNeighbor(new Edge(p.getEntry2(), p.getSimilarity()));
            p.getEntry2().addNeighbor(new Edge(p.getEntry1(), p.getSimilarity()));
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

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    @Override
    public double getProgress() {
        return (totalProgress == 0 ? 0 : 100.0*currentProgress/totalProgress);
    }
}
