package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.Filters;

import java.util.regex.Pattern;

public class DataSetEntry {
    private static final Pattern tab_regex = Pattern.compile("\t");
    private String tweet;
    private Class classification;

    public DataSetEntry(String line, int tweetIndex, int classIndex) {
        String[] values = tab_regex.split(line);
        tweet = values[tweetIndex];
        classification = Class.parseClassificationFromString(values[classIndex]);
    }


    public String getTweet() {
        return tweet;
    }

    public Class getClassification() {
        return classification;
    }

    public void applyFilters(Filters filters) {
        tweet = filters.apply(tweet);
    }

    public enum Class {
        POSITIVE, NEUTRAL, NEGATIVE;

        public static Class parseClassificationFromString(String classification) {
            switch (classification) {
                case "positive":
                    return Class.POSITIVE;
                case "neutral":
                    return Class.NEUTRAL;
                case "negative":
                    return Class.NEGATIVE;
                default:
                    throw new IllegalArgumentException("Unknown class: " + classification);
            }
        }

        public static Class classifyFromThresholds(double value, double lowThresh, double highThresh) {
            if(value < lowThresh) {
                return NEGATIVE;
            } else if(value > highThresh) {
                return POSITIVE;
            } else {
                return NEUTRAL;
            }
        }

        public boolean isPositive() {
            return this == POSITIVE;
        }

        public boolean isNeutral() {
            return this == NEUTRAL;
        }

        public boolean isNegative() {
            return this == NEGATIVE;
        }
    }
}
