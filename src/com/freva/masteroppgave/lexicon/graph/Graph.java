package com.freva.masteroppgave.lexicon.graph;

import java.util.*;

public class Graph {
    private HashMap<String, Integer> polarityLexiconWords;
    private ArrayList<Node> nodes = new ArrayList<>();
    private static final float edgeThreshold = 0.3f;
    private static final int pathLength = 3;
    private static final int neighborLimit = 30;

    /**
     *
     * @param node to add to graph
     */
    public void addNode(Node node) {
        nodes.add(node);
    }


    /**
     * Initializes a HashMap containing all prior polarity words existing in the graph.
     * @param polarityLexiconWords - A HashMap containing the polarity words in the graph. Ex.:{"good" : 3, "bad" : -3, ...}
     */
    public void setPolarityLexiconWords(HashMap<String, Integer> polarityLexiconWords) {
        this.polarityLexiconWords = polarityLexiconWords;
    }

    /**
     * Comparing each node, checking if there should be an edge between them
     */
    public void createAndWeighEdges() {
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                checkIfEdge(nodes.get(i), nodes.get(j));
            }
        }
    }

    /**
     * Compares a pair of nodes and adds them as neighbors if their similarity is larger than set edgeThreshold
     * @param node1 - first node to be compared
     * @param node2 - second node to be compared
     */
    private void checkIfEdge(Node node1, Node node2) {
        String[][] contextVector1 = node1.getContextVector();
        String[][] contextVector2 = node2.getContextVector();
        double leftSimilarity = calculateSimilarity(createVectors(contextVector1[0], contextVector2[0]));
        double rightSimilarity = calculateSimilarity(createVectors(contextVector1[1], contextVector2[1]));
        double similarity = Math.max(leftSimilarity, rightSimilarity);
        if (similarity >= edgeThreshold) {
            node1.addNeighbor(new Edge(node2, similarity));
            node2.addNeighbor(new Edge(node1, similarity));
        }
    }

    /**
     * Calculates the Cosine Similarity between two context vectors
     * @param vectors - A HashMap containing numerical vectors for two context vectors
     * @return The calculated Cosine Similarity
     */
    private double calculateSimilarity(HashMap<String, int[]> vectors) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (String key : vectors.keySet()) {
            dotProduct += vectors.get(key)[0] * vectors.get(key)[1];
            normA += Math.pow(vectors.get(key)[0], 2);
            normB += Math.pow(vectors.get(key)[1], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Creates a HashMap containing numerical vectors for two given context vectors.
     * Ex. {"cool": [1, 0], "nice": [0, 1] ...}
     * 1 represents the occurrence of the word in the respective context vector.
     * The first element in the Array represents the first given context vector and the second the second given context vector.
     * @param contextVector1 - The first context vector
     * @param contextVector2 - The second context vector
     * @return The HashMap containing the numerical vectors.
     */
    private HashMap<String, int[]> createVectors(String[] contextVector1, String[] contextVector2) {
        ArrayList<String[]> contextVectors = new ArrayList<>();
        contextVectors.add(contextVector1);
        contextVectors.add(contextVector2);
        HashMap<String, int[]> occurrences = new HashMap<>();
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < contextVectors.get(j).length; i++) {
                if (!occurrences.containsKey(contextVectors.get(j)[i])) {
                    int[] frequencies = new int[2];
                    if(contextVectors.get(j)[i] != null) {
                        frequencies[j] += 1;
                    }
                    occurrences.put(contextVectors.get(j)[i], frequencies);
                } else {
                    int[] frequencies = occurrences.get(contextVectors.get(j)[i]);
                    if(contextVectors.get(j)[i] != null) {
                        frequencies[j] += 1;
                    }
                    occurrences.put(contextVectors.get(j)[i], frequencies);
                }
            }
        }
        return occurrences;
    }

    /**
     * Propagates sentiment scores from nodes with prior polarity values to nodes on a path with length = pathLength from the node.
     * The prior polarity nodes propagates its sentiment score to its neighbors, the neighbors then propagates to their neighbors and so on until the pathLength is reached.
     * Each node only propagates sentiment to its x = neighborLimit highest weighted neighbors.
     */
    public void propagateSentiment() {
        for(Node node : nodes) {
            if(polarityLexiconWords.containsKey(node.getPhrase())) {
                ArrayList<Node> nodesToCheck = new ArrayList<>();
                nodesToCheck.add(node);
                node.updateSentimentScore((double)polarityLexiconWords.get(node.getPhrase()), node);
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
        }
    }

    /**
     * Returns all nodes in the graph
     * @return - All nodes
     */
    public ArrayList<Node> getNodes() {
        return nodes;
    }
}
