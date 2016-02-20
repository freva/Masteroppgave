package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.utils.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetNGrams;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONLineByLine;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.util.function.Function;


public class Initialization {
    private static final File tweets_file = new File("res/tweets/200k.txt");
    private static final File ngrams_file = new File("res/tweets/ngrams.txt");
    private static final File afinn_file = new File("res/data/afinn111.json");
    private static final boolean use_cached_ngrams = false;
    private static boolean asd = true;

    public static void main(String args[]) throws Exception{
        Map<String, Integer> ngrams;
        if(! use_cached_ngrams) {
            ngrams = getAndCacheFrequentNGrams(tweets_file, ngrams_file, 6, 0.00025,
                    Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
                    Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername, Filters::removeEmoticons,
                    Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
                    Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);
        } else {
            ngrams = FileUtils.readObjectFromJSONFile(ngrams_file, new TypeToken<Map<String, Integer>>(){});
        }


        TweetReader tweetReader = new TweetReader(tweets_file,
                Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalTextPlus,
                Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);
        ProgressBar.trackProgress(tweetReader, "Reading in tweets...");
        final String[] tweets = tweetReader.readAndPreprocessAllTweets();

        Graph graph = initializeGraph(tweets);
        Map<String, Double> lexicon = createLexicon(graph);
        writeLexiconToFile("res/tweets/lexicon.txt", lexicon);
    }


    @SafeVarargs
    private static Map<String, Integer> getAndCacheFrequentNGrams(File input, File output, int n, double frequencyCutoff,
                                                                  Function<String, String>... filters) throws IOException {
        TweetNGrams tweetNGrams = new TweetNGrams();
        ProgressBar.trackProgress(tweetNGrams, "Generating tweet n-grams...");
        Map<String, Integer> ngrams = tweetNGrams.getFrequentNGrams(input, n, frequencyCutoff, filters);
        ngrams = MapUtils.sortMapByValue(ngrams);

        FileUtils.writeObjectToFileAsJSON(ngrams, output, true);
        return ngrams;
    }


    /**
     * Creation of two phrase-vectors(left of phrase and right of phrase) using a set of tweets containing the phrase.
     * The phrase-vectors should contain the x = phraseVectorSize most frequent words used together with the phrase.
     * @throws IOException
     */
    private static Graph initializeGraph(String[] tweets) throws IOException {
        JSONLineByLine<Map<String, List<Integer>>> ngrams = new JSONLineByLine<>(ngrams_file, new TypeToken<Map<String, List<Integer>>>(){});
        ProgressBar.trackProgress(ngrams, "Initializing graph...");
        Graph graph = new Graph();

        while(ngrams.hasNext()) {
            Map.Entry<String, List<Integer>> entry = ngrams.next().entrySet().iterator().next();
            graph.addPhrase(entry.getKey());
            for(int i = 0; i < entry.getValue().size(); i++) {
                graph.updatePhraseContext(entry.getKey(), tweets[entry.getValue().get(i)]);
            }
        }

        return graph;
    }



    /**
     * Initializes a graph of phrases, before starting the sentiment propagation within the graph resulting in a sentiment lexicon.
     * @throws IOException
     */
    private static Map<String, Double> createLexicon(Graph graph) throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(afinn_file);
        graph.setPriorPolarityLexicon(priorPolarityLexicon);

        ProgressBar.trackProgress(graph, "Creating and weighing edges...");
        graph.createAndWeighEdges();
        ProgressBar.trackProgress(graph, "Propagating Sentiment...");
        graph.propagateSentiment();

        return graph.getLexicon();
    }


    private static void writeLexiconToFile(String filename, Map<String, Double> lexicon) throws IOException {
        Map<String, Double> sortedLexicon = MapUtils.sortMapByValue(lexicon);
        try(Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"))) {
            for (Map.Entry<String, Double> entry : sortedLexicon.entrySet()) {
                output.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        }
    }
}
