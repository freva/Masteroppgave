package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.utils.tools.NGrams;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;


public class TweetNGrams implements Progressable {
    private TweetReader tweetReader;

    /**
     * Finds all frequent n-grams in a file, treating each new line as a new document.
     * @param input File with documents to generate n-grams for
     * @param n Maximum n-gram length
     * @param frequencyCutoff Smallest required frequency to include n-gram
     * @param filters List of filters to apply to document before generating n-grams
     * @return Map of n-grams as key and number of occurrences as value
     * @throws IOException
     */
    public final Map<String, Integer> getFrequentNGrams(File input, int n, double frequencyCutoff, List<Function<String, String>> filters) throws IOException {
        this.tweetReader = new TweetReader(input, filters);
        Map<String, Integer> nGramsCounter = new HashMap<>();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");
        int lineCounter;

        for(lineCounter=0; tweetReader.hasNext(); lineCounter++) {
            if(lineCounter % 50000 == 0 && lineCounter != 0) {
                MapUtils.removeInfrequentItems(nGramsCounter, (int) (frequencyCutoff * lineCounter) / 2);
            }

            String line = tweetReader.readAndPreprocessNextTweet();
            for(String[] nGramTokens: NGrams.getSyntacticalNGrams(line, n)) {
                String nGram = StringUtils.join(nGramTokens, " ");
                if(! containsAlphabet.matcher(nGram).find()) continue;
                if(WordFilters.containsIntensifier(nGramTokens) || WordFilters.containsNegation(nGramTokens)) continue;
                if(WordFilters.isStopWord(nGramTokens[0]) || WordFilters.isStopWord(nGramTokens[nGramTokens.length - 1])) continue;

                MapUtils.incrementMapByValue(nGramsCounter, nGram, 1);
            }
        }

        MapUtils.removeInfrequentItems(nGramsCounter, (int) (frequencyCutoff*lineCounter));
        return nGramsCounter;
    }


    @Override
    public double getProgress() {
        return tweetReader != null ? tweetReader.getProgress() : 0;
    }
}