package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.graph.Node;
import com.freva.masteroppgave.lexicon.container.ContextScore;
import com.freva.masteroppgave.utils.similarity.PairSimilarity;
import com.freva.masteroppgave.utils.similarity.Cosine;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetContexts;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetNGrams;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONLineByLine;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.util.function.Function;


public class Initialization {
    private static final File tweets_file = new File("res/tweets/1m.txt");
    private static final File ngrams_file = new File("res/tweets/ngrams.txt");
    private static final File context_file = new File("res/tweets/context.txt");
    private static final File lexicon_file = new File("res/tweets/lexicon.txt");
    private static final File afinn_file = new File("res/data/afinn111.json");

    private static final Boolean use_cached_ngrams = true;
    private static final Boolean use_cached_contexts = true;

    private static final int max_n_grams_range = 6;
    private static final int max_context_word_distance = 4;
    private static final double n_grams_cut_off_frequency = 0.0025;

    private static final int neighborLimit = 30;
    private static final int pathLength = 3;
    private static final float edgeThreshold = 0.3f;

    public static void main(String args[]) throws Exception{
        if(! use_cached_contexts) {
            Map<String, Integer> ngrams;
            if (!use_cached_ngrams) {
                ngrams = getAndCacheFrequentNGrams(max_n_grams_range, n_grams_cut_off_frequency,
                        Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
                        Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername, Filters::removeEmoticons,
                        Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
                        Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);
            } else {
                String JSONNGrams = FileUtils.readEntireFileIntoString(ngrams_file);
                ngrams = JSONUtils.fromJSON(JSONNGrams, new TypeToken<Map<String, Integer>>(){});
            }

            TweetContexts tweetContexts = new TweetContexts();
            ProgressBar.trackProgress(tweetContexts, "Finding context words...");
            tweetContexts.findContextWords(tweets_file, context_file, ngrams.keySet(), max_context_word_distance,
                    Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                    Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                    Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalTextPlus,
                    Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);
        }

        Graph graph = initializeGraph();
        Map<String, Integer> lexicon = createLexicon(graph);
        lexicon = MapUtils.sortMapByValue(lexicon);
        String jsonLexicon = JSONUtils.toJSON(lexicon, true);
        FileUtils.writeToFile(lexicon_file, jsonLexicon);
    }


    @SafeVarargs
    private static Map<String, Integer> getAndCacheFrequentNGrams(int n, double frequencyCutoff,
                                                                  Function<String, String>... filters) throws IOException {
        TweetNGrams tweetNGrams = new TweetNGrams();
        ProgressBar.trackProgress(tweetNGrams, "Generating tweet n-grams...");
        Map<String, Integer> ngrams = tweetNGrams.getFrequentNGrams(tweets_file, n, frequencyCutoff, filters);
        ngrams = MapUtils.sortMapByValue(ngrams);

        String JSONNGrams = JSONUtils.toJSON(ngrams, true);
        FileUtils.writeToFile(ngrams_file, JSONNGrams);
        return ngrams;
    }


    /**
     * Creation of two phrase-vectors(left of phrase and right of phrase) using a set of tweets containing the phrase.
     * The phrase-vectors should contain the x = phraseVectorSize most frequent words used together with the phrase.
     * @throws IOException
     */
    private static Graph initializeGraph() throws IOException {
        JSONLineByLine<Map<String, Map<String, Integer>>> contexts = new JSONLineByLine<>(context_file, new TypeToken<Map<String, Map<String, Integer>>>(){});
        ProgressBar.trackProgress(contexts, "Initializing graph...");
        Graph graph = new Graph(neighborLimit, pathLength, edgeThreshold);

        while(contexts.hasNext()) {
            Map<String, Map<String, Integer>> map = contexts.next();
            ContextScore contextScore = new ContextScore(map);
            for(Map.Entry<String, String> pair: contextScore.getContextPairs()) {
                ContextScore.Score score = contextScore.getScore(pair.getKey(), pair.getValue());
                graph.updatePhraseContext(pair.getKey(), pair.getValue(), score.getLeftScore(), score.getRightScore());
            }
        }

        return graph;
    }



    /**
     * Initializes a graph of phrases, before starting the sentiment propagation within the graph resulting in a sentiment lexicon.
     * @throws IOException
     */
    private static Map<String, Integer> createLexicon(Graph graph) throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(afinn_file);
        graph.setPriorPolarityLexicon(priorPolarityLexicon);

        Cosine<Node> cosine = new Cosine<>();
        ProgressBar.trackProgress(cosine, "Calculating cosine similarities...");
        List<PairSimilarity<Node>> similarities = graph.getSimilarities(cosine);
        graph.createEdges(similarities);

        ProgressBar.trackProgress(graph, "Propagating Sentiment...");
        graph.propagateSentiment();

        return graph.getLexicon();
    }
}
