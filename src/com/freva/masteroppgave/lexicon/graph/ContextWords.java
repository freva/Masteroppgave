package com.freva.masteroppgave.lexicon.graph;


import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContextWords {
    private Map<String, Double> leftSideContextWords;
    private Map<String, Double> rightSideContextWords;

    public ContextWords(Map<String, Double> leftSideContextWords, Map<String, Double> rightSideContextWords) {
        this.leftSideContextWords = leftSideContextWords;
        this.rightSideContextWords = rightSideContextWords;
    }

    public Map<String, Double> getLeftSideContextWords() {
        return leftSideContextWords;
    }

    public Map<String, Double> getRightSideContextWords() {
        return rightSideContextWords;
    }

    public double getSimilarity(ContextWords other) {
        double leftSimilarity = calculateSimilarity(other.getLeftSideContextWords(), leftSideContextWords);
        double rightSimilarity = calculateSimilarity(other.getRightSideContextWords(), rightSideContextWords);
        return Math.max(leftSimilarity, rightSimilarity);
    }

    /**
     * Calculates the Cosine Similarity between two context vectors
     * @param other - A HashMap containing numerical vectors for two context vectors
     * @return The calculated Cosine Similarity
     */
    public static double calculateSimilarity(Map<String, Double> other, Map<String, Double> current) {
        HashMap<String, double[]> vectors = createVectors(other, current);
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
     * @param other - The second context vector
     * @return The HashMap containing the numerical vectors.
     */
    public static HashMap<String, double[]> createVectors(Map<String, Double> other, Map<String, Double> current) {
        ArrayList<Map<String, Double>> contextVectors = new ArrayList<>();
        contextVectors.add(other);
        contextVectors.add(current);
        HashMap<String, double[]> occurrences = new HashMap<>();
        for (int j = 0; j < 2; j++) {
            for (String word : contextVectors.get(j).keySet()) {
                if (!occurrences.containsKey(word)) {
                    double[] frequencies = new double[2];
                    if (word != null) {
                        frequencies[j] += 1+ contextVectors.get(j).get(word);
                    }
                    occurrences.put(word, frequencies);
                } else {
                    double[] frequencies = occurrences.get(word);
                    if (word != null) {
                        frequencies[j] += contextVectors.get(j).get(word);
                    }
                    occurrences.put(word, frequencies);
                }
            }
        }
        return occurrences;
    }
}
