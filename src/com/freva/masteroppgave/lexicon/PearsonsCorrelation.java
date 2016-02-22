package com.freva.masteroppgave.lexicon;

import java.util.*;
import java.util.List;
import java.util.stream.IntStream;


public class PearsonsCorrelation {
    private Map<String, Integer> wordMappings;
    private double[][] cosines;

    public PearsonsCorrelation(int[][] coOccurrences, List<String> keys) throws Exception {
        wordMappings = getWordMappings(keys);

        double[][] normalized = normalize(coOccurrences);
        cosines = calculateCosines(normalized);
    }

    public double getLeftSimilarityBetween(String word1, String word2) {
        return cosines[wordMappings.get(word1)][2 * wordMappings.get(word2)];
    }

    public double getRightSimilarityBetween(String word1, String word2) {
        return cosines[wordMappings.get(word1)][2 * wordMappings.get(word2) + 1];
    }


    private static Map<String, Integer> getWordMappings(List<String> words) {
        Map<String, Integer> indexMapper = new HashMap<>();
        for(int i=0; i<words.size(); i++) {
            indexMapper.put(words.get(i), i);
        }
        return indexMapper;
    }


    //First index: row | Second index: column
    private static double[][] normalize(int[][] values) {
        int T = sumArray(values);
        double[][] normalized = new double[values.length][values[0].length];

        int[] columnSums = IntStream.range(0, values.length).map(i -> sumColumn(values, i)).toArray();
        int[] rowSums = IntStream.range(0, values[0].length).map(i -> sumRow(values, i)).toArray();

        for(int row = 0; row<values.length; row++) {
            //normalized[row] = new double[row+1];
            for(int column = 0; column<values[0].length; column++) {
                int over = T * values[row][column] - rowSums[row] * columnSums[column];
                int under = rowSums[row]*(T-rowSums[row])*columnSums[column]*(T-columnSums[column]);
                normalized[row][column] = over / Math.sqrt(under);
                normalized[row][column] = normalized[row][column] > 0 ? Math.sqrt(normalized[row][column]) : 0;
            }
        }
        return normalized;
    }


    private static double[][] calculateCosines(double[][] normalized) {
        double[] rowSqSum = Arrays.stream(normalized)
                .mapToDouble(row -> Math.sqrt(cross(row, row)))
                .toArray();

        double[][] cosines = new double[normalized.length][normalized[0].length];
        for(int row = 0; row<normalized.length; row++) {
            for(int col = 0; col<row; col++) {
                double mul = cross(normalized[row], normalized[col]);
                double cosine = mul / (rowSqSum[row] * rowSqSum[col]);
                cosines[row][col] = Double.isNaN(cosine) ? 0 : cosine;
            }
        }

        return cosines;
    }

    private static double cross(double[] a, double[] b) {
        double sum = 0;
        for(int i=0; i<a.length; i++) {
            if(a[i] == 0 || b[i] == 0) continue;
            sum += a[i] * b[i];
        }
        return sum;
    }


    private static int sumArray(int[][] array) {
        return IntStream.range(0, array.length).map(i -> sumColumn(array, i)).sum();
    }

    private static int sumColumn(int[][] array, int column) {
        return IntStream.range(0, array.length).map(i -> array[i][column]).sum();
    }

    private static int sumRow(int[][] array, int row) {
        return Arrays.stream(array[row]).sum();
    }
}