package com.freva.masteroppgave.utils.similarity;

import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.util.*;
import java.util.stream.IntStream;


public class Cosine<T> implements Progressable {
    private int totalProgress;
    private int currentProgress = 0;

    public List<PairSimilarity<T>> getSimilarities(int[][] coOccurrences, List<T> entries){
        totalProgress = (coOccurrences.length*(coOccurrences.length-1))/2;
        double[][] normalized = normalize(coOccurrences);
        return calculateCosines(normalized, entries);
    }

    //First index: row | Second index: column
    private static double[][] normalize(int[][] values) {
        int T = sumArray(values);
        double[][] normalized = new double[values.length][values[0].length];

        int[] columnSums = IntStream.range(0, values[0].length).map(i -> sumColumn(values, i)).toArray();
        int[] rowSums = IntStream.range(0, values.length).map(i -> sumRow(values, i)).toArray();

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


    private List<PairSimilarity<T>> calculateCosines(double[][] normalized, List<T> entries) {
        double[] rowSqSum = Arrays.stream(normalized)
                .mapToDouble(row -> Math.sqrt(cross(row, row)))
                .toArray();

        List<PairSimilarity<T>> similarities = new ArrayList<>();
        for(int row = 0; row<normalized.length; row++) {
            for(int col = 0; col<row; col++, currentProgress++) {
                double mul = cross(normalized[row], normalized[col]);
                double cosine = mul / (rowSqSum[row] * rowSqSum[col]);
                double similarity = Double.isNaN(cosine) ? 0 : cosine;
                similarities.add(new PairSimilarity<>(entries.get(row), entries.get(col), similarity));
            }
        }

        return similarities;
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

    @Override
    public double getProgress() {
        return (totalProgress == 0 ? 0 : 100.0*currentProgress/totalProgress);
    }
}