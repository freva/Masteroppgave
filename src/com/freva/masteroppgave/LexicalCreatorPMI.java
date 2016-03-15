package com.freva.masteroppgave;

import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.preprocessing.filters.CharacterCleaner;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetNGramsPMI;
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
    public static final List<Function<String, String>> N_GRAM_STRING_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::removeHashtag, Filters::placeholderUsername, Filters::removeEmoticons,
            Filters::removeFreeDigits, String::toLowerCase);
    public static final List<Function<String, String>> N_GRAM_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalText);
    public static final Filters N_GRAM_FILTERS = new Filters(N_GRAM_STRING_FILTERS, N_GRAM_CHARACTER_FILTERS);

    public static final List<Function<String, String>> TWEET_STRING_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, CharacterCleaner::unicodeEmotesToAlias, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::protectHashtag, Filters::removeEMail, Filters::removeUsername,
            Filters::removeFreeDigits, Filters::replaceEmoticons, String::toLowerCase);
    public static final List<Function<String, String>> TWEET_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText);
    public static final Filters TWEET_FILTERS = new Filters(TWEET_STRING_FILTERS, TWEET_CHARACTER_FILTERS);

    private DataSetReader dataSetReader;

    private static final int nGramRange = 4;
    private static final int nGramFrequencyThreshold = 40;
    private static final double cutoffFrequency = 0.0002;
    private static final Boolean useCachedNGrams = true;


    public static void main(String[] args) throws IOException {
        Set<String> frequentNGrams = generateNGrams();
        TokenTrie tokenTrie = new TokenTrie(frequentNGrams);

        LexicalCreatorPMI lexicalCreatorPMI = new LexicalCreatorPMI();
        ProgressBar.trackProgress(lexicalCreatorPMI, "Creating lexicon...");
        lexicalCreatorPMI.createLexicon(tokenTrie);
    }


    public void createLexicon(TokenTrie tokenTrie) throws IOException {
        dataSetReader = new DataSetReader(Resources.CLASSIFIED, 1, 0);

        Map<String, Integer> wordsPos = new HashMap<>();
        Map<String, Integer> wordsNeg = new HashMap<>();
        Parallel.For(dataSetReader, entry -> {
            String tweet = TWEET_FILTERS.apply(entry.getTweet());
            List<String> tokens = tokenTrie.findOptimalTokenization(RegexFilters.WHITESPACE.split(tweet));

            for(String nGram : tokens) {
                String[] nGramWords = RegexFilters.WHITESPACE.split(nGram);
                if(containsIllegalWord(nGramWords)) continue;

                if(entry.getClassification().isPositive()){
                    MapUtils.incrementMapByValue(wordsPos, nGram, 1);
                } else {
                    MapUtils.incrementMapByValue(wordsNeg, nGram, 1);
                }
            }
        });

        int pos = wordsPos.values().stream().mapToInt(Integer::valueOf).sum();
        int neg = wordsNeg.values().stream().mapToInt(Integer::valueOf).sum();
        double total = pos + neg;
        final double ratio = (double) neg / pos;

        Map<String, Double> lexicon = new HashMap<>();
        Set<String> allKeys = new HashSet<>(wordsPos.keySet());
        allKeys.retainAll(wordsNeg.keySet());
        for(String key : allKeys){
            if((wordsNeg.getOrDefault(key, 0) + wordsPos.getOrDefault(key, 0)) / total > 0.00001) {
                int over = wordsPos.getOrDefault(key, 1);
                int under = wordsNeg.getOrDefault(key, 1);

                double sentimentValue = Math.log(ratio * over / under);
                if(Math.abs(sentimentValue) >= 0.5) lexicon.put(key, sentimentValue);
            }
        }

        lexicon = MapUtils.normalizeMapBetween(lexicon, -5, 5);
//        lexicon = findAdjectives(lexicon);
        JSONUtils.toJSONFile(Resources.PMI_LEXICON, MapUtils.sortMapByValue(lexicon), true);
    }

    private static Set<String> generateNGrams() throws IOException {
        if(! useCachedNGrams) {
            TweetNGramsPMI tweetNGrams = new TweetNGramsPMI(); //new File("res/tweets/filtered1.txt")
            ProgressBar.trackProgress(tweetNGrams, "Generating tweet n-grams...");
            Map<String, Double> ngrams = tweetNGrams.getFrequentNGrams(new File("res/tweets/filtered1.txt"), nGramRange, cutoffFrequency, N_GRAM_FILTERS);
            ngrams = MapUtils.sortMapByValue(ngrams);

            JSONUtils.toJSONFile(Resources.TEMP_NGRAMS, ngrams, true);
            return ngrams.keySet();
        } else {
            return JSONUtils.fromJSONFile(Resources.TEMP_NGRAMS, new TypeToken<HashMap<String, Double>>(){}).keySet();
        }
    }

    private Map<String, Double> findAdjectives(Map<String, Double> lexicon) throws IOException {
        String adjectiveJson = FileUtils.readEntireFileIntoString(new File("res/tweets/adjectiveDict.txt"));
        Map<String, String[]> adjectiveDict = JSONUtils.fromJSON(adjectiveJson, new TypeToken<HashMap<String, String[]>>(){});
        HashMap<String, Double> wordsToBeAdded = new HashMap<>();
        for(String key : lexicon.keySet()){
            if(adjectiveDict.containsKey(key)){
                for(String relatedWord : adjectiveDict.get(key)){
                    if(! lexicon.containsKey(relatedWord) && ! wordsToBeAdded.containsKey(relatedWord)){
                        wordsToBeAdded.put(relatedWord, lexicon.get(key));
                    }
                }
            }
        }
        return MapUtils.mergeMaps(lexicon, wordsToBeAdded);
    }


    private static boolean containsIllegalWord(String[] nGram) {
        return WordFilters.isStopWord(nGram[nGram.length - 1])|| WordFilters.containsIntensifier(nGram);
    }


    @Override
    public double getProgress() {
        return dataSetReader == null ? 0 : dataSetReader.getProgress();
    }
}
