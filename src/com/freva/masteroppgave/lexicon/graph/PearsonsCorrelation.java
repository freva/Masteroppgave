package com.freva.masteroppgave.lexicon.graph;


import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.MapUtils;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;


public class PearsonsCorrelation {
    private static final int[] weights = new int[]{1, 2, 3, 4, 0, 4, 3, 2, 1};
    private static final int windowSize = 4;


    public static void main(String[] args) throws IOException {
        String text = FileUtils.readEntireFileIntoString("res/tweets/pg100.txt");
        text = Filters.chain(text,
                Filters::HTMLUnescape, Filters::normalizeForm, Filters::removeURL, Filters::removeInnerWordCharacters,
                Filters::removeNonAlphabeticText, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);
        PearsonsCorrelation.getCosines(text);
    }

    public static void getCosines(String test) {
        long startTime = System.currentTimeMillis();

        String[] words = RegexFilters.WHITESPACE.split(test.toLowerCase());
        String[] sortedWords = getSortedWords(words, 2000);
        System.out.println("Tracking " + sortedWords.length + " words. Found in: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");
        startTime = System.currentTimeMillis();

        Map<String, Integer> indexMapper = getWordMappings(sortedWords);
        int[][] coOccurrences = getCoOccurrences(words, indexMapper);
        System.out.println("Generated co-occurrences matrix in: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");
        startTime = System.currentTimeMillis();

        double[][] normalized = normalize(coOccurrences);
        System.out.println("Normalized matrix in: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");
        startTime = System.currentTimeMillis();

        Map<Point, Double> cosines = calculateCosines(normalized);
        System.out.println("Calculated cosine similarities: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");


        int top = 100;
        cosines = MapUtils.sortMapByValue(cosines);
        for(Map.Entry<Point, Double> entry: cosines.entrySet()) {
            if(top-- < 0) break;
            Point p = entry.getKey();
            System.out.println(sortedWords[(int) p.getX()] + " + " + sortedWords[(int) p.getY()] + ": " + String.format("%.3f", entry.getValue()));
        }
    }

    private static String[] getSortedWords(String[] words, int limit) {
        HashMap<String, Integer> counter = new HashMap<>();
        for(String word: words) {
            MapUtils.incrementMapValue(counter, word);
        }
        Map<String, Integer> map = MapUtils.getNLargest(counter, limit);
        String[] sorted = map.keySet().toArray(new String[Math.min(map.size(), limit)]);
        Arrays.sort(sorted);
        return sorted;
    }

    private static Map<String, Integer> getWordMappings(String[] sorted) {
        Map<String, Integer> indexMapper = new HashMap<>();
        for(int i=0; i<sorted.length; i++) {
            indexMapper.put(sorted[i], i);
        }
        return indexMapper;
    }

    private static int[][] getCoOccurrences(String[] words, Map<String, Integer> indexMapper) {
        int[][] coOccurrence = new int[indexMapper.size()][indexMapper.size()];
        for(int wordIndex = 0; wordIndex<words.length; wordIndex++) {
            for(int offset=-windowSize; offset<=windowSize; offset++) {
                if (offset == 0) continue;
                if(wordIndex+offset>=0 && wordIndex+offset<words.length &&
                        indexMapper.containsKey(words[wordIndex]) && indexMapper.containsKey(words[wordIndex+offset])) {
                    int currentWordIndex = indexMapper.get(words[wordIndex]);
                    int neighborWordIndex = indexMapper.get(words[wordIndex+offset]);
                    coOccurrence[currentWordIndex][neighborWordIndex] += weights[4+offset];
                }
            }
        }

        return coOccurrence;
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


    private static Map<Point, Double> calculateCosines(double[][] normalized) {
        double[] rowSqSum = Arrays.stream(normalized)
                .mapToDouble(row -> IntStream.range(0, row.length).mapToDouble(col -> row[col] * row[col]).sum())
                .toArray();

        Map<Point, Double> cosines = new HashMap<>();
        for(int row = 0; row<normalized.length; row++) {
            for(int column = 0; column<row; column++) {
                double mul = cross(normalized[row], normalized[column]);
                cosines.put(new Point(row, column), mul / Math.sqrt(rowSqSum[row]*rowSqSum[column]));
            }
        }

        return cosines;
    }

    private static double cross(double[] a, double[] b) {
        return IntStream.range(0, a.length).mapToDouble(i -> a[i]*b[i]).sum();
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