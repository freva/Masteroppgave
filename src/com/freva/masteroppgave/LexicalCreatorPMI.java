package com.freva.masteroppgave;

import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.util.*;

public class LexicalCreatorPMI implements Progressable{
    private DataSetReader dataSetReader;

    private Map<String, Double> createLexicon(DataSetReader dataSetReader, Collection<String> nGrams, double maxErrorRate,
                                              double sentimentValueThreshold, Filters filters, Map<String, String[]> adjectives) {
        Map<String, Double> lexicon = createLexicon(dataSetReader, nGrams, maxErrorRate, sentimentValueThreshold, filters);
        Map<String, Double> wordsToBeAdded = new HashMap<>();
        lexicon.keySet().stream()
                .filter(adjectives::containsKey)
                .forEach(key -> {
            for (String relatedWord : adjectives.get(key)) {
                if (!lexicon.containsKey(relatedWord) && !wordsToBeAdded.containsKey(relatedWord)) {
                    wordsToBeAdded.put(relatedWord, lexicon.get(key));
                }
            }
        });

        return MapUtils.mergeMaps(lexicon, wordsToBeAdded);
    }


    public Map<String, Double> createLexicon(DataSetReader dataSetReader, Collection<String> nGrams, double maxErrorRate,
                                             double sentimentValueThreshold, Filters filters) {
        this.dataSetReader = dataSetReader;
        TokenTrie tokenTrie = new TokenTrie(nGrams);

        Map<String, Integer> wordsPos = new HashMap<>();
        Map<String, Integer> wordsNeg = new HashMap<>();
        Parallel.For(dataSetReader, entry -> {
            String tweet = filters.apply(entry.getTweet());
            List<String> tokens = tokenTrie.findOptimalTokenization(RegexFilters.WHITESPACE.split(tweet));

            for(String nGram : tokens) {
                String[] nGramWords = RegexFilters.WHITESPACE.split(nGram);
                if(containsIllegalWord(nGramWords)) continue;

                if(entry.getClassification().isPositive()){
                    MapUtils.incrementMapByValue(wordsPos, nGram, 1);
                } else if (entry.getClassification().isNegative()) {
                    MapUtils.incrementMapByValue(wordsNeg, nGram, 1);
                }
            }
        });

        int pos = wordsPos.values().stream().mapToInt(Integer::valueOf).sum();
        int neg = wordsNeg.values().stream().mapToInt(Integer::valueOf).sum();

        final double ratio = (double) neg / pos;
        final double Z = 2.5759; //Two nines
        final double cutoff = Z * Z / 4 / maxErrorRate / maxErrorRate;

        Map<String, Double> lexicon = new HashMap<>();
        Set<String> allKeys = new HashSet<>(wordsPos.keySet());
        allKeys.retainAll(wordsNeg.keySet());
        for(String key : allKeys){
            if (wordsNeg.getOrDefault(key, 0) + wordsPos.getOrDefault(key, 0) > cutoff) {
                int over = wordsPos.getOrDefault(key, 1);
                int under = wordsNeg.getOrDefault(key, 1);

                double sentimentValue = Math.log(ratio * over / under);
                if (Math.abs(sentimentValue) >= sentimentValueThreshold) lexicon.put(key, sentimentValue);
            }
        }

        return MapUtils.normalizeMapBetween(lexicon, -5, 5);
    }


    private static boolean containsIllegalWord(String[] nGram) {
        return ClassifierOptions.isStopWord(nGram[nGram.length - 1])|| ClassifierOptions.containsIntensifier(nGram);
    }


    @Override
    public double getProgress() {
        return dataSetReader == null ? 0 : dataSetReader.getProgress();
    }
}
