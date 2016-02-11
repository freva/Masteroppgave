package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.graph.Node;
import com.freva.masteroppgave.lexicon.utils.PriorPolarityLexicon;
import com.freva.masteroppgave.lexicon.utils.TweetReader;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.JSONLineByLine;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class Initialization {
    private static final HashMap<String, String[][]> phraseInTweets = new HashMap<>();
    private static String[] tweets;

    private static final int phraseVectorSize = 10;
    private static final int phraseWindowSize = 6;
    private static final Pattern punctuation = Pattern.compile("[!?.]");


    public static void main(String args[]) throws Exception{
        tweets = TweetReader.readAndPreprocessTweets("res/tweets/10k.txt",
                Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalTextPlus,
                Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

        createFinalHashMap();
        createGraph();
    }


    /**
     * Creation of two phrase-vectors(left of phrase and right of phrase) using a set of tweets containing the phrase.
     * The phrase-vectors should contain the x = phraseVectorSize most frequent words used together with the phrase.
     * @throws IOException
     */
    private static void createFinalHashMap() throws IOException {
        JSONLineByLine<Map<String, Integer[]>> ngrams = new JSONLineByLine<>("res/tweets/ngrams.txt", new TypeToken<Map<String, Integer[]>>(){}.getType());
        while(ngrams.hasNext()) {
            Map.Entry<String, Integer[]> entry = ngrams.next().entrySet().iterator().next();
            HashMap<String, Integer> [] wordFrequencies = new HashMap[]{new HashMap(), new HashMap()};
            for(int i = 0; i < entry.getValue().length; i++) {
                String tweet = tweets[entry.getValue()[i]];
                String filteredTweet = Filters.removeNonAlphanumericalText(tweet);
                if(filteredTweet.contains(entry.getKey())) {
                    String[] phraseWindows = constructPhraseWindows(tweet, entry.getKey());
                    for(int j = 0; j < phraseWindows.length; j++) {
                        for(String word : phraseWindows[j].trim().split(" ")) {
                            int count = wordFrequencies[j].containsKey(word) ? wordFrequencies[j].get(word) : 0;
                            wordFrequencies[j].put(word, count + 1);
                        }
                    }
                }
            }
            String[][] phraseVectors = new String[2][phraseVectorSize];
            for(int i = 0; i < wordFrequencies.length; i++) {
                Map<String, Integer> sortedWordFrequency = MapUtils.sortMapByValue(wordFrequencies[i]);
                int counter = 0;
                String[] relatedWords = new String[phraseVectorSize];
                for (String sortedKey : sortedWordFrequency.keySet()) {
                    if(WordFilters.containsStopWord(sortedKey)) continue;
                    if (counter < phraseVectorSize) {
                        relatedWords[counter] = sortedKey;
                    } else {
                        break;
                    }
                    counter++;
                }
                phraseVectors[i] = relatedWords;
            }
            phraseInTweets.put(entry.getKey(), phraseVectors);
        }
    }


    //Finds where the phrase starts and then creates a String phraseWindow containing the 2 words in front of the phrase,
    //the phrase, and then the 2 following words. Ugly code needs cleanup

    /**
     * Finds all occurrences of the phrase withing the given tweet and creates two Strings(phraseWindows).
     * The phraseWindows contains the x = phraseWindowSize words in front of the phrase and the x words following the phrase respectively.
     * Ex: Tweet = "I really don't like that guy", Phrase = "don't like", PhraseWindowSize = 2  -> phraseWindows = ["I really don't like", "don't like that guy"]
     * @param tweet - The tweet the phrase occurs in.
     * @param phrase - The given phrase.
     * @return - A string array containing the two phraseWindows.
     */
    private static String[] constructPhraseWindows(String tweet, String phrase) {
        String[] tweetParts = punctuation.split(tweet);
        String[] keyWords = phrase.split(" ");
        String[] phraseWindows = {"",""};
        ArrayList<int[]> indexes = new ArrayList<>();
        for(int j = 0; j < tweetParts.length; j++) {
            String[] wordsInTweet = tweetParts[j].split(" ");
            for (int i = 0; i < wordsInTweet.length; i++) {
                if (matchesAtIndex(wordsInTweet, keyWords, i)) {
                    int[] indexEntry = {j, i};
                    indexes.add(indexEntry);
                }
            }
        }
        for(int i = 0; i < indexes.size(); i++) {
            for (int j = indexes.get(i)[1] - phraseWindowSize; j <= indexes.get(i)[1] + (keyWords.length - 1) + phraseWindowSize; j++) {
                String[] wordsInTweet = tweetParts[indexes.get(i)[0]].split(" ");
                if (j >= 0 && j < wordsInTweet.length && !wordsInTweet[j].equals("_")) {
                    if (j <= indexes.get(i)[1] + (keyWords.length - 1)) {
                        phraseWindows[0] += wordsInTweet[j] + " ";
                    }
                    if (j >= indexes.get(i)[1]) {
                        phraseWindows[1] += wordsInTweet[j] + " ";
                    }
                }
            }
        }
        return phraseWindows;
    }

    /**
     * Checks if a given index is the starting index of a phrase in a tweet.
     * @param tweet - The given tweet.
     * @param phrase - The given phrase.
     * @param index - The current index.
     * @return True if correct index, False else.
     */
    private static boolean matchesAtIndex(String[] tweet, String[] phrase, int index) {
        for(int j = 0 ; j < phrase.length && index+j < tweet.length; j++) {
            if(!tweet[index+j].equals(phrase[j])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initializes a graph of phrases, before starting the sentiment propagation within the graph resulting in a sentiment lexicon.
     * @throws IOException
     */
    private static void createGraph() throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon("res/data/afinn111.json");
        Graph graph = new Graph();
        System.out.println("\nAdding nodes...");
        HashMap<String, Integer> polarityLexiconWords = new HashMap<>();
        for(String key : phraseInTweets.keySet()) {
            graph.addNode(new Node(key, phraseInTweets.get(key)));
            int polarityValue = priorPolarityLexicon.getPolarity(key);
            if(polarityValue != 0) {
                polarityLexiconWords.put(key, polarityValue);
            }
        }
        graph.setPolarityLexiconWords(polarityLexiconWords);
        System.out.println("Creating and weighing edges...");
        graph.createAndWeighEdges();
        System.out.println("Propagating Sentiment...");
        graph.propagateSentiment();

        ArrayList<Node> nodes = graph.getNodes();
        Collections.sort(nodes);
        for(int i = 0; i < nodes.size(); i++) {
            System.out.println(nodes.get(i).getPhrase() + " : " + nodes.get(i).getSentimentScore());
        }
    }
}
