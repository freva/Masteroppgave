package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.lexicon.container.ContextScore;
import com.freva.masteroppgave.lexicon.container.PhraseTree;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.reader.TweetReader;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.google.gson.reflect.TypeToken;

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
    public final void findContextWords(File input, File output, Set<String> tracked, int cutOffDistance, List<Function<String, String>> filters) throws IOException {
        this.tweetReader = new TweetReader(input, filters);
        PhraseTree tree = new PhraseTree(tracked);
        TypeToken typeToken = new TypeToken<ContextScore>(){};

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            for(String tweet: tweetReader) {
                ContextScore trackedDistances = getTrackedDistances(tweet, tree, cutOffDistance);
                String JSONTrackedDistances = JSONUtils.toJSON(trackedDistances, typeToken, false);
                writer.write(JSONTrackedDistances + "\n");
            }
        }
    }


    private static ContextScore getTrackedDistances(String line, PhraseTree tree, int cutOffDistance) {
        ContextScore contextScore = new ContextScore();

        for(String sentence: RegexFilters.SENTENCE_END_PUNCTUATION.split(line)) {
            String[] sentenceTokens = RegexFilters.WHITESPACE.split(sentence);
            List<PhraseTree.Phrase> phrases = tree.findTrackedWords(sentenceTokens);

            for (int i = 0; i < phrases.size(); i++) {
                PhraseTree.Phrase p1 = phrases.get(i);
                for (int j = 0; j < i; j++) {
                    PhraseTree.Phrase p2 = phrases.get(j);
                    if (p1.overlapsWith(p2)) continue;

                    int score = getScoreBetweenPoints(p1, p2, cutOffDistance);
                    if (score == 0) continue;
                    contextScore.addDistance(p1.getPhrase(), p2.getPhrase(), score);
                }
            }
        }

        return contextScore;
    }


    /**
     * Calculates context score between two ranges (end of the first range to start of the second range)
     * @return Context score between ranges
     */
    private static int getScoreBetweenPoints(PhraseTree.Phrase p1, PhraseTree.Phrase p2, int cutOffDistance) {
        int distance1 = p2.getEndIndex()-p1.getStartIndex(), distance2 = p2.getStartIndex()-p1.getEndIndex();
        int absDist1 = Math.abs(distance1), absDist2 = Math.abs(distance2);
        if(absDist1 < absDist2) {
            return (int) Math.signum(distance1) * (cutOffDistance + 1 - absDist1);
        } else {
            return (int) Math.signum(distance2) * (cutOffDistance + 1 - absDist2);
        }
    }


    @Override
    public double getProgress() {
        return tweetReader != null ? tweetReader.getProgress() : 0;
    }
}
