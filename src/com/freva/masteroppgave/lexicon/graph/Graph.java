package com.freva.masteroppgave.lexicon.graph;

import java.util.*;

public class Graph {
    private ArrayList<Node> nodes = new ArrayList<>();
    private static final float edgeThreshold = 0.6f;

    /**
     *
     * @param node to add to graph
     */
    public void addNode(Node node) {
        nodes.add(node);
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
        String[] contextVector1 = node1.getContextVector();
        String[] contextVector2 = node2.getContextVector();
        double similarity = calculateSimilarity(contextVector1, contextVector2);
        if (similarity >= edgeThreshold) {
            node1.addNeighbor(new Edge(node2, similarity));
            node2.addNeighbor(new Edge(node1, similarity));
        }
    }

    /**
     * Calculates the Cosine Similarity between two context vectors
     * @param contextVector1 - The first context vector
     * @param contextVector2 - The second context vector
     * @return The calculated Cosine Similarity
     */
    private double calculateSimilarity(String[] contextVector1, String[] contextVector2) {
        HashMap<String, int[]> vectors = createVectors(contextVector1, contextVector2);
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
                    frequencies[j] += 1;
                    occurrences.put(contextVectors.get(j)[i], frequencies);
                } else {
                    int[] frequencies = occurrences.get(contextVectors.get(j)[i]);
                    frequencies[j] += 1;
                    occurrences.put(contextVectors.get(j)[i], frequencies);
                }
            }
        }
        return occurrences;
    }

    /**
     * Returns all nodes in the graph
     * @return - All nodes
     */
    public ArrayList<Node> getNodes() {
        return nodes;
    }
}
