package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.lexicon.utils.PriorPolarityLexicon;
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

    /**
     *
     * @param phrase Phrase to add to graph
     */
    public void addPhrase(String phrase) {
        nodes.put(phrase, new Node(phrase));
    }


    public void updatePhraseContext(String phrase, String context) {
        nodes.get(phrase).updatePhraseContext(context);
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
        List<Node> nodeList = new ArrayList<>(getNodes());
        totalProgress = nodeList.size() * (nodeList.size()-1) / 2;
        currentProgress = 0;

        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                checkIfEdge(nodeList.get(i), nodeList.get(j));
                currentProgress++;
            }
        }
    }

    /**
     * Compares a pair of nodes and adds them as neighbors if their similarity is larger than set edgeThreshold
     * @param node1 - first node to be compared
     * @param node2 - second node to be compared
     */
    private void checkIfEdge(Node node1, Node node2) {
        ContextWords cw1 = node1.getContextWords();
        ContextWords cw2 = node2.getContextWords();
//        double leftSimilarity = calculateSimilarity(createVectors(cw1.getLeftSideContextWords(), cw2.getLeftSideContextWords()));
//        double rightSimilarity = calculateSimilarity(createVectors(cw1.getRightSideContextWords(), cw2.getRightSideContextWords()));
        double similarity = cw1.getSimilarity(cw2);
//        double similarity = Math.max(leftSimilarity, rightSimilarity);
        if (similarity >= edgeThreshold) {
            node1.addNeighbor(new Edge(node2, similarity));
            node2.addNeighbor(new Edge(node1, similarity));
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
        for(Node node : nodes.values()) {
            if(priorPolarityLexicon.hasWord(node.getPhrase())) {
                ArrayList<Node> nodesToCheck = new ArrayList<>();
                nodesToCheck.add(node);
                node.updateSentimentScore((double) priorPolarityLexicon.getPolarity(node.getPhrase()), node);
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
        for(Node node: getNodes()) {
            lexicon.put(node.getPhrase(), node.getSentimentScore());
        }

        return lexicon;
    }

    /**
     * Returns all nodes in the graph
     * @return - All nodes
     */
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    @Override
    public double getProgress() {
        return (totalProgress == 0 ? 0 : 100.0*currentProgress/totalProgress);
    }
}
