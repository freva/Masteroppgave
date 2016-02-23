package com.freva.masteroppgave.preprocessing.preprocessors;

import java.util.regex.Pattern;

public class DataSetEntry {
    private static final Pattern tab_regex = Pattern.compile("\t");
    private String tweet;
    private Class classification;

    public DataSetEntry(String line, int tweetIndex, int classIndex) {
        String[] values = tab_regex.split(line);
        tweet = values[tweetIndex];
        classification = Class.getClassificationFromString(values[classIndex]);
    }


    public String getTweet() {
        return tweet;
    }

    public Class getClassification() {
        return classification;
    }


    public enum Class {
        POSITIVE, NEUTRAL, NEGATIVE;

        public static Class getClassificationFromString(String classification) {
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
    }
}
