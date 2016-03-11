package com.freva.masteroppgave;

import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.lexicon.container.TokenTrie.*;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.*;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.Parallel;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;


public class LexicalCreatorPMI implements Progressable{
    private DataSetReader dataSetReader;
    private TokenTrie<String> tokenTrie;
    private final int nGramRange = 3;
    private final double cutoffFrequency = 0.0004;
    private final int nGramFrequencyThreshold = 50;
    private boolean useCachedNGrams = false;

    public static final List<Function<String, String>> TWEET_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::hashtagToWord, Filters::removeUsername, Filters::replaceEmoticons,
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
            Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

    public static final List<Function<String, String>> N_GRAM_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername, Filters::removeEmoticons,
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
            Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);


    public static void main(String[] args) throws IOException {
        LexicalCreatorPMI lexicalCreatorPMI = new LexicalCreatorPMI();
        ProgressBar.trackProgress(lexicalCreatorPMI, "Creating lexicon...");
        lexicalCreatorPMI.createLexicon();
    }


    public void createLexicon() throws IOException {
        dataSetReader = new DataSetReader(new File("res/tweets/classified.txt"), 1, 0);
        generateNGrams();
        Map<String, Integer> wordsPos = new HashMap<>();
        Map<String, Integer> wordsNeg = new HashMap<>();
        Parallel.For(dataSetReader, entry -> {
            String tweet = Filters.chain(entry.getTweet(), TWEET_FILTERS);
            List<TokenTrie<String>.Token> tokens = tokenTrie.findOptimalAllocation(RegexFilters.WHITESPACE.split(tweet));
            for(Token token : tokens) {
                String[] nGramWords = (String[]) token.getTokenSequence();
                if(containsIllegalWord(nGramWords)) continue;
                String nGram = String.join(" ", nGramWords);
                if(entry.getClassification().isPositive()){
                    MapUtils.incrementMapByValue(wordsPos, nGram, 1);
                } else {
                    MapUtils.incrementMapByValue(wordsNeg, nGram, 1);
                }
            }
        });

        int pos = wordsPos.values().stream().mapToInt(Integer::valueOf).sum();
        int neg = wordsNeg.values().stream().mapToInt(Integer::valueOf).sum();
        final double ratio = (double) neg / pos;

        Map<String, Double> lexicon = new HashMap<>();
        for(String key : wordsPos.keySet()){
            if(wordsNeg.getOrDefault(key, 0) > nGramFrequencyThreshold || wordsPos.getOrDefault(key, 0) > nGramFrequencyThreshold) {
                int over = wordsPos.getOrDefault(key, 1);
                int under = wordsNeg.getOrDefault(key, 1);

                double sentimentValue = Math.log(ratio * over / under);
                lexicon.put(key, sentimentValue);
            }
        }

        JSONUtils.toJSONFile(Resources.PMI_LEXICON, MapUtils.sortMapByValue(lexicon), true);
    }

    private void generateNGrams() throws IOException {
        if(!useCachedNGrams) {
            LexicalCreator.getAndCacheFrequentNGrams(nGramRange, cutoffFrequency,N_GRAM_FILTERS);
        }
        String nGramsJson = FileUtils.readEntireFileIntoString(Resources.TEMP_NGRAMS);
        Set<String> ngrams = JSONUtils.fromJSON(nGramsJson, new TypeToken<HashMap<String, Integer>>(){}).keySet();
        tokenTrie = TokenTrie.createTrieFromSentences(ngrams);
    }


    private boolean containsIllegalWord(String[] nGram) {
        return WordFilters.isStopWord(nGram[nGram.length - 1]) || WordFilters.containsNegation(nGram) || WordFilters.containsIntensifier(nGram);
    }


    @Override
    public double getProgress() {
        return dataSetReader == null ? 0 : dataSetReader.getProgress();
    }
}
