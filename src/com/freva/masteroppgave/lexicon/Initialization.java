package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.utils.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetsNGrams;
import com.freva.masteroppgave.utils.JSONLineByLine;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;


public class Initialization {
    private static final String tweets_file = "res/tweets/10k.txt";
    private static final boolean generate_ngrams = true;

    public static void main(String args[]) throws Exception{
        if(generate_ngrams) {
            TweetsNGrams tweetsNGrams = new TweetsNGrams();
            ProgressBar.trackProgress(tweetsNGrams, "Generating tweet n-grams...");
            tweetsNGrams.createNGrams(tweets_file, "res/tweets/ngrams.txt", 0.002);
        }

        TweetReader tweetReader = new TweetReader();
        ProgressBar.trackProgress(tweetReader, "Reading in tweets...");
        final String[] tweets = tweetReader.readAndPreprocessTweets(tweets_file,
                Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalTextPlus,
                Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

        Graph graph = initializeGraph(tweets);
        Map<String, Double> lexicon = createLexicon(graph);
        writeLexiconToFile("res/tweets/lexicon.txt", lexicon);
    }


    /**
     * Creation of two phrase-vectors(left of phrase and right of phrase) using a set of tweets containing the phrase.
     * The phrase-vectors should contain the x = phraseVectorSize most frequent words used together with the phrase.
     * @throws IOException
     */
    private static Graph initializeGraph(String[] tweets) throws IOException {
        JSONLineByLine<Map<String, List<Integer>>> ngrams = new JSONLineByLine<>("res/tweets/ngrams.txt", new TypeToken<Map<String, List<Integer>>>(){}.getType());
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
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon("res/data/afinn111.json");
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
