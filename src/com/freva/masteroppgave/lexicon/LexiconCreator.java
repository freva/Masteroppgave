package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.container.Adjectives;
import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.util.*;

public class LexiconCreator implements Progressable {
    private DataSetReader dataSetReader;

    public Map<String, Double> createLexicon(DataSetReader dataSetReader, Collection<String> nGrams, double minTotalOccurrences,
                                             double sentimentValueThreshold, Filters filters) {
        Map<String, Counter> counter = countNGramsByPolarity(dataSetReader, nGrams, filters);
        Map<String, Double> lexicon = new HashMap<>();

        final int pos = counter.values().stream().mapToInt(i -> i.numPositive).sum();
        final int neg = counter.values().stream().mapToInt(i -> i.numNegative).sum();
        final double ratio = (double) neg / pos;

        counter.entrySet().stream()
                .filter(entry -> entry.getValue().getTotalOccurrences() > minTotalOccurrences)
                .forEach(entry -> {
                    int over = entry.getValue().numPositive;
                    int under = entry.getValue().numNegative;

                    double sentimentValue = Math.log(ratio * over / under);
                    if (Math.abs(sentimentValue) >= sentimentValueThreshold) {
                        lexicon.put(entry.getKey(), sentimentValue);

                        if (RegexFilters.WHITESPACE.split(entry.getKey()).length == 1 && !ClassifierOptions.isSpecialClassWord(entry.getKey())) {
                            for (String relatedWord : Adjectives.getAdverbAndAdjectives(entry.getKey())) {
                                if (counter.containsKey(relatedWord) && !lexicon.containsKey(relatedWord)) {
                                    lexicon.put(relatedWord, sentimentValue);
                                }
                            }
                        }
                    }
                });

        return MapUtils.normalizeMapBetween(lexicon, -5, 5);
    }


    private Map<String, Counter> countNGramsByPolarity(DataSetReader dataSetReader, Collection<String> nGrams, Filters filters) {
        this.dataSetReader = dataSetReader;
        TokenTrie tokenTrie = new TokenTrie(nGrams);

        Map<String, Counter> counter = new HashMap<>();
        Parallel.For(dataSetReader, entry -> {
            String tweet = filters.apply(entry.getTweet());
            List<String> tokens = tokenTrie.findOptimalTokenization(RegexFilters.WHITESPACE.split(tweet));

            for (String nGram : tokens) {
                String[] nGramWords = RegexFilters.WHITESPACE.split(nGram);
                if (containsIllegalWord(nGramWords)) continue;
                if (!counter.containsKey(nGram)) counter.put(nGram, new Counter());

                if (entry.getClassification().isPositive()) {
                    counter.get(nGram).numPositive++;
                } else if (entry.getClassification().isNegative()) {
                    counter.get(nGram).numNegative++;
                }
            }
        });

        return counter;
    }

    private static boolean containsIllegalWord(String[] nGram) {
        return ClassifierOptions.isStopWord(nGram[nGram.length - 1]) || ClassifierOptions.containsIntensifier(nGram);
    }

    public double getProgress() {
        return dataSetReader == null ? 0 : dataSetReader.getProgress();
    }

    private class Counter {
        private int numPositive = 4;
        private int numNegative = 4;

        private int getTotalOccurrences() {
            return numPositive + numNegative;
        }
    }
}
