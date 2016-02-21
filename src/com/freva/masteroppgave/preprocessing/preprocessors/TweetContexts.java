package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.lexicon.utils.ContextDistance;
import com.freva.masteroppgave.lexicon.utils.PhraseTree;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class TweetContexts implements Progressable {
    private TweetReader tweetReader;

    /**
     * Finds all context words to tracked words in a file, treating each new line as a new document.
     * @param input File with documents to generate contexts for
     * @param output File to write context words to
     * @param tracked Set of tracked words to generate context for
     * @param cutOffDistance Maximum distance between words to be considered context neighbors
     * @param filters List of filters to apply to document before generating context
     * @throws IOException
     */
    @SafeVarargs
    public final void findContextWords(File input, File output, Set<String> tracked, int cutOffDistance, Function<String, String>... filters) throws IOException {
        this.tweetReader = new TweetReader(input, filters);
        PhraseTree tree = new PhraseTree(tracked);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            while(tweetReader.hasNext()) {
                String tweet = tweetReader.readAndPreprocessNextTweet();
                ContextDistance trackedDistances = getTrackedDistances(tweet, tree, cutOffDistance);
                String JSONTrackedDistances = JSONUtils.toJSON(trackedDistances, false);
                writer.write(JSONTrackedDistances + "\n");
            }
        }
    }


    private static ContextDistance getTrackedDistances(String line, PhraseTree tree, int cutOffDistance) {
        String[] tokens = RegexFilters.WHITESPACE.split(line);
        Map<Point, String> trackedWords = findTrackedWords(tokens, tree);
        List<Point> phraseBounds = new ArrayList<>(trackedWords.keySet());
        ContextDistance contextDistance = new ContextDistance();

        for(int i=0; i<phraseBounds.size(); i++) {
            Point p1 = phraseBounds.get(i);
            for(int j=0; j<i; j++) {
                Point p2 = phraseBounds.get(j);
                if(rangesOverlap(p1, p2)) continue;

                int distance = getDistanceBetweenPoints(p1, p2);
                if(Math.abs(distance) > cutOffDistance) continue;
                contextDistance.addDistance(trackedWords.get(p1), trackedWords.get(p2), distance);
            }
        }
        return contextDistance;
    }

    private static Map<Point, String> findTrackedWords(String[] tokens, PhraseTree tree) {
        Map<Point, String> trackedWords = new HashMap<>();

        for(int i=0; i<tokens.length; i++) {
            for(int j=i+1; j<tokens.length; j++) {
                String[] phrase = Arrays.copyOfRange(tokens, i, j);
                Boolean status = tree.hasPhrase(phrase);

                if(status == null) {
                    continue;
                } if(status.equals(true)) {
                    trackedWords.put(new Point(i, j-1), String.join(" ", phrase));
                } else if(status.equals(false)) {
                    break;
                }
            }
        }
        return trackedWords;
    }


    /**
     * Checks if two ranges overlap
     * @param p1 First range (from x to and including y)
     * @param p2 Second range
     * @return true if overlap, false otherwise
     */
    private static boolean rangesOverlap(Point p1, Point p2) {
        return p1.getX() <= p2.getY() && p2.getX() <= p1.getY();
    }


    /**
     * Calculates distance between two ranges (end of the first range to start of the second range)
     * @return Distance between ranges
     */
    private static int getDistanceBetweenPoints(Point p1, Point p2) {
        int distance1 = (int) (p2.getY()-p1.getX()), distance2 = (int) (p2.getX()-p1.getY());
        if(Math.abs(distance1) < Math.abs(distance2)) {
            return distance1;
        } else {
            return distance2;
        }
    }


    @Override
    public double getProgress() {
        return tweetReader != null ? tweetReader.getProgress() : 0;
    }
}
