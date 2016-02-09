package com.freva.masteroppgave.lexicon.graph;

import java.util.*;

public class Graph {
    private ArrayList<Node> nodes = new ArrayList<>();
    private static final float edgeThreshold = 0.6f;

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void createAndWeighEdges() {
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                checkIfEdge(nodes.get(i), nodes.get(j));
            }
        }
    }

    private void checkIfEdge(Node node1, Node node2) {
        String[] tweets1 = node1.getPhraseVector();
        String[] tweets2 = node2.getPhraseVector();
        double similarity = calculateSimilarity(tweets1, tweets2);
        if (similarity >= edgeThreshold) {
            node1.addNeighbor(new Edge(node2, similarity));
            node2.addNeighbor(new Edge(node1, similarity));
        }
    }

    //    Calculates the CosineSimilarity between two word vectors.
    private double calculateSimilarity(String[] tweet1, String[] tweet2) {
        HashMap<String, int[]> vectors = createVectors(tweet1, tweet2);
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

    private HashMap<String, int[]> createVectors(String[] tweet1, String[] tweet2) {
        ArrayList<String[]> tweets = new ArrayList<>();
        tweets.add(tweet1);
        tweets.add(tweet2);
        HashMap<String, int[]> occurrences = new HashMap<>();
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < tweets.get(j).length; i++) {
                if (!occurrences.containsKey(tweets.get(j)[i])) {
                    int[] frequencies = new int[2];
                    frequencies[j] += 1;
                    occurrences.put(tweets.get(j)[i], frequencies);
                } else {
                    int[] frequencies = occurrences.get(tweets.get(j)[i]);
                    frequencies[j] += 1;
                    occurrences.put(tweets.get(j)[i], frequencies);
                }
            }
        }
        return occurrences;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
}
