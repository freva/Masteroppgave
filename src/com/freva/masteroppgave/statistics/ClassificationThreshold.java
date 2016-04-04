package com.freva.masteroppgave.statistics;

import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;

import java.util.*;

public class ClassificationThreshold {
    private List<SentimentTuple> previousResults = new ArrayList<>();
    private double lowThreshold;
    private double highThreshold;
    private double maxAccuracy;
    private boolean cacheUpToDate = false;

    public synchronized void updateEvidence(DataSetEntry.Class correctClass, double predictedSentiment) {
        previousResults.add(new SentimentTuple(correctClass, predictedSentiment));
        cacheUpToDate = false;
    }


    private void updateThresholds() {
        Collections.sort(previousResults, Collections.reverseOrder());
        int[][] counters = new int[DataSetEntry.Class.values().length][previousResults.size()];

        counters[previousResults.get(0).correct.ordinal()][0]++;
        for(int i=1; i<previousResults.size(); i++) {
            for(int j=0; j<counters.length; j++) {
                counters[j][i] = counters[j][i-1];
            }
            counters[previousResults.get(i).correct.ordinal()][i]++;
        }

        for(int i=1; i<previousResults.size()-2; i++) {
            for (int j=i; j<previousResults.size()-1; j++) {
                double tempAccuracy = getAccuracy(counters, i, j);

                if(tempAccuracy > maxAccuracy) {
                    maxAccuracy = tempAccuracy;
                    lowThreshold = (previousResults.get(i-1).predicted + previousResults.get(i).predicted) / 2;
                    highThreshold = (previousResults.get(j).predicted + previousResults.get(j+1).predicted) / 2;
                }
            }
        }

        cacheUpToDate = true;
    }


    /**
     * Calculates optimal lower bound for neutral class. Optimizes based on accuracy.
     * @return Lower bound threshold
     */
    public synchronized double getLowThreshold() {
        if(! cacheUpToDate) updateThresholds();
        return lowThreshold;
    }

    /**
     * Calculates optimal higher bound for neutral class. Optimizes based on accuracy.
     * @return Higher bound threshold
     */
    public synchronized double getHighThreshold() {
        if(! cacheUpToDate) updateThresholds();
        return highThreshold;
    }

    /**
     * Calculates accuracy achieved if the optimal bounds were used.
     * @return Accuracy measure, value within [0, 1]
     */
    public double getMaxAccuracy() {
        return maxAccuracy;
    }


    private double getAccuracy(int[][] counters, int start, int end) {
        int correctNegative = counters[DataSetEntry.Class.NEGATIVE.ordinal()][start-1];
        int correctPositive = counters[DataSetEntry.Class.POSITIVE.ordinal()][previousResults.size()-1] - counters[DataSetEntry.Class.POSITIVE.ordinal()][end+1];
        int correctNeutral = counters[DataSetEntry.Class.NEUTRAL.ordinal()][end] - counters[DataSetEntry.Class.NEUTRAL.ordinal()][start];

        return (double) (correctNegative+correctNeutral+correctPositive) / previousResults.size();
    }

    private class SentimentTuple implements Comparable<SentimentTuple> {
        private DataSetEntry.Class correct;
        private double predicted;

        SentimentTuple(DataSetEntry.Class correct, double predicted) {
            this.correct = correct;
            this.predicted = predicted;
        }

        public String toString() {
            return "[" + correct.name() + ", " + predicted + "]";
        }

        @Override
        public int compareTo(SentimentTuple o) {
            double diff = o.predicted - predicted;
            return diff != 0 ? (int) Math.signum(diff) : o.correct.compareTo(correct);
        }
    }
}
