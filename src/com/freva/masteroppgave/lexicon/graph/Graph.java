package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.lexicon.container.*;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.similarity.Cosine;
import com.freva.masteroppgave.utils.similarity.PairSimilarity;
import com.freva.masteroppgave.utils.tools.FixedPriorityQueue;

import java.util.*;

public class Graph implements Progressable {
    private HashMap<String, Node> nodes = new HashMap<>();
    private PriorPolarityLexicon priorPolarityLexicon;

    private int neighborLimit;
    private int pathLength;
    private double edgeThreshold;
    private double beta;

    private int currentProgress = 0;
    private int totalProgress = 0;

    public Graph(int neighborLimit, int pathLength, double edgeThreshold) {
        this.neighborLimit = neighborLimit;
        this.pathLength = pathLength;
        this.edgeThreshold = edgeThreshold;
    }


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
    public List<PairSimilarity<Node>> getSimilarities(Cosine<Node> cosine) {
        List<Node> nodeList = new ArrayList<>(nodes.values());
        int[][] coOccurrences = new int[nodes.size()][nodes.size()*2];
        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = 0; j < nodeList.size(); j++ ) {
                coOccurrences[i][j*2] = nodeList.get(i).getLeftScoreForWord(nodeList.get(j).getPhrase());
                coOccurrences[i][j*2+1] = nodeList.get(j).getRightScoreForWord(nodeList.get(j).getPhrase());
            }
            int max = Arrays.stream(coOccurrences[i]).max().getAsInt()/2;
            coOccurrences[i][2*i] = max;
            coOccurrences[i][2*i+1] = max;
        }

        return cosine.getSimilarities(coOccurrences, nodeList);
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
        Set<String> subjectiveNGrams = priorPolarityLexicon.getSubjectiveWords();
        subjectiveNGrams.retainAll(nodes.keySet());

        currentProgress = 0;
        totalProgress = subjectiveNGrams.size();

        for(String subjectiveNGram: subjectiveNGrams) {
            Node subjectiveNode = nodes.get(subjectiveNGram);
            LinkedList<Node> nodesToCheck = new LinkedList<>(Collections.singletonList(subjectiveNode));

            subjectiveNode.updateSentimentScore(subjectiveNode, priorPolarityLexicon.getPolarity(subjectiveNGram));
            for (int i = 0; i < pathLength; i++) {
                for (int size=nodesToCheck.size(); size > 0; size--) {
                    Node nodeToCheck = nodesToCheck.pop();
                    FixedPriorityQueue<Edge> neighbors = new FixedPriorityQueue<>(neighborLimit, nodeToCheck.getNeighbors());

                    for (Edge edge : neighbors.sortedItems()) {
                        edge.getNeighbor().updateSentimentScore(nodeToCheck, nodeToCheck.getCurrentScore()*edge.getWeight());
                        nodesToCheck.addLast(edge.getNeighbor());
                    }
                }
            }

            currentProgress++;
        }

        double sumPos = nodes.values().parallelStream().mapToDouble(Node::getPositiveSentimentScore).sum();
        double sumNeg = nodes.values().parallelStream().mapToDouble(Node::getNegativeSentimentScore).sum();
        beta = sumPos/sumNeg;
    }


    public Map<String, Double> getLexicon(double sentimentThreshold) {
        Map<String, Double> lexicon = new HashMap<>();
        for(Map.Entry<String, Node> entry: nodes.entrySet()) {
            double sentiment = entry.getValue().getSentimentScore(beta);

            if(Math.abs(sentiment) > sentimentThreshold) {
                lexicon.put(entry.getKey(), sentiment);
            }
        }

        return lexicon;
    }


    @Override
    public double getProgress() {
        return (totalProgress == 0 ? 0 : 100.0*currentProgress/totalProgress);
    }
}
