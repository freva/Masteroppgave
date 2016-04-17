package com.freva.masteroppgave.utils.reader;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.reader.DataSetReader.DataSetEntry;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

public class DataSetReader implements Iterator<DataSetEntry>, Iterable<DataSetEntry>, Progressable {
    private static final Pattern tab_regex = Pattern.compile("\t");

    private final LineReader lineReader;
    private final int tweetIndex;
    private final int classIndex;

    public DataSetReader(File file, int tweetIndex, int classIndex) throws IOException {
        lineReader = new LineReader(file);
        this.tweetIndex = tweetIndex;
        this.classIndex = classIndex;
    }

    public boolean hasNext() {
        return lineReader.hasNext();
    }

    public DataSetEntry next() {
        return new DataSetEntry(lineReader.next(), tweetIndex, classIndex);
    }

    public Iterator<DataSetEntry> iterator() {
        return this;
    }

    public double getProgress() {
        return lineReader.getProgress();
    }


    public class DataSetEntry {
        private final Classification classification;
        private String tweet;

        public DataSetEntry(String line, int tweetIndex, int classIndex) {
            String[] values = tab_regex.split(line);
            tweet = values[tweetIndex];
            classification = Classification.parseClassificationFromString(values[classIndex]);
        }


        public String getTweet() {
            return tweet;
        }

        public Classification getClassification() {
            return classification;
        }

        public void applyFilters(Filters filters) {
            tweet = filters.apply(tweet);
        }
    }


    public enum Classification {
        POSITIVE, NEUTRAL, NEGATIVE;

        public static Classification parseClassificationFromString(String classification) {
            switch (classification) {
                case "positive":
                    return Classification.POSITIVE;
                case "neutral":
                    return Classification.NEUTRAL;
                case "negative":
                    return Classification.NEGATIVE;
                default:
                    throw new IllegalArgumentException("Unknown class: " + classification);
            }
        }

        public static Classification classifyFromThresholds(double value, double lowThresh, double highThresh) {
            if (value < lowThresh) {
                return NEGATIVE;
            } else if (value > highThresh) {
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
