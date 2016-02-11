package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.utils.PriorPolarityLexicon;
import com.freva.masteroppgave.lexicon.utils.TweetReader;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.utils.JSONLineByLine;
import com.freva.masteroppgave.utils.MapUtils;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;


public class Initialization {

    public static void main(String args[]) throws Exception{

        final long startTime = System.currentTimeMillis();
        final String[] tweets = TweetReader.readAndPreprocessTweets("res/tweets/10k.txt",
                Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalTextPlus,
                Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

        Graph graph = initializeGraph(tweets);
        Map<String, Double> lexicon = createLexicon(graph);
        writeLexiconToFile("res/tweets/lexicon.txt", lexicon);

        System.out.println("In: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");
    }


    /**
     * Creation of two phrase-vectors(left of phrase and right of phrase) using a set of tweets containing the phrase.
     * The phrase-vectors should contain the x = phraseVectorSize most frequent words used together with the phrase.
     * @throws IOException
     */
    private static Graph initializeGraph(String[] tweets) throws IOException {
        JSONLineByLine<Map<String, Integer[]>> ngrams = new JSONLineByLine<>("res/tweets/ngrams.txt", new TypeToken<Map<String, Integer[]>>(){}.getType());
        Graph graph = new Graph();

        while(ngrams.hasNext()) {
            Map.Entry<String, Integer[]> entry = ngrams.next().entrySet().iterator().next();
            graph.addPhrase(entry.getKey());
            for(int i = 0; i < entry.getValue().length; i++) {
                graph.updatePhraseContext(entry.getKey(), tweets[entry.getValue()[i]]);
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

        System.out.println("Creating and weighing edges...");
        graph.createAndWeighEdges();
        System.out.println("Propagating Sentiment...");
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
