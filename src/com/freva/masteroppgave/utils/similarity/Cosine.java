package com.freva.masteroppgave.utils.similarity;

import com.freva.masteroppgave.utils.ArrayUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.util.*;
import java.util.stream.IntStream;


public class Cosine<T> implements Progressable {
    private int totalProgress;
    private int currentProgress = 0;


    /**
     * Calculates cosine similarity between elements in 2D-array
     * @param coOccurrences Elements array, first dimension for elements, second for element values
     * @param entries Entries being compared, assumed same order as rows
     * @return List of PairSimilarities storing similarity for each pair of nodes
     */
    public List<PairSimilarity<T>> getSimilarities(int[][] coOccurrences, List<T> entries){
        totalProgress = (coOccurrences.length*(coOccurrences.length-1))/2;
        double[][] normalized = normalize(coOccurrences);
        return calculateCosines(normalized, entries);
    }


    /**
     * Performs Pearson Correlation normalization on weighted co-occurrence array
     * @param values Co-occurrence array
     * @return Normalized co-occurrence array
     */
    private static double[][] normalize(int[][] values) {
        int T = ArrayUtils.sum(values);
        double[][] normalized = new double[values.length][values[0].length];

        int[] columnSums = IntStream.range(0, values[0].length).map(i -> ArrayUtils.sumColumn(values, i)).toArray();
        int[] rowSums = IntStream.range(0, values.length).map(i -> ArrayUtils.sumRow(values, i)).toArray();

        for(int row = 0; row<values.length; row++) {
            for(int column = 0; column<values[0].length; column++) {
                int over = T * values[row][column] - rowSums[row] * columnSums[column];
                int under = rowSums[row]*(T-rowSums[row])*columnSums[column]*(T-columnSums[column]);
                double temp = over / Math.sqrt(under);

                if(temp > 0) {
                    normalized[row][column] = Math.sqrt(temp);
                }
            }
        }
        return normalized;
    }


    private List<PairSimilarity<T>> calculateCosines(double[][] normalized, List<T> entries) {
        double[] rowSqSum = Arrays.stream(normalized)
                .mapToDouble(row -> Math.sqrt(ArrayUtils.dotProduct(row, row)))
                .toArray();

        List<PairSimilarity<T>> similarities = new ArrayList<>();
        for(int row = 0; row<normalized.length; row++) {
            for(int col = 0; col<row; col++, currentProgress++) {
                double mul = ArrayUtils.dotProduct(normalized[row], normalized[col]);
                double cosine = mul / (rowSqSum[row] * rowSqSum[col]);
                double similarity = Double.isNaN(cosine) ? 0 : cosine;
                similarities.add(new PairSimilarity<>(entries.get(row), entries.get(col), similarity));
            }
        }

        return similarities;
    }


    @Override
    public double getProgress() {
        return (totalProgress == 0 ? 0 : 100.0*currentProgress/totalProgress);
    }
}