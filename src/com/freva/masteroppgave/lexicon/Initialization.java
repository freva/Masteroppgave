package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.graph.Node;
import com.freva.masteroppgave.lexicon.utils.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.JSONLineByLine;
import com.freva.masteroppgave.utils.ProgressBar;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class Initialization {
    private static final ArrayList<String> tweets = new ArrayList<>();
    private static final HashMap<String, String[][]> phraseInTweets = new HashMap<>();

    private static final int phraseVectorSize = 10;
    private static final int phraseWindowSize = 6;
    private static final Pattern punctuation = Pattern.compile("[!?.]");


    public static void main(String args[]) throws Exception{
        readTweets();
        createFinalHashMap();
        createGraph();
    }


    private static void readTweets() throws IOException {
        System.out.println("Reading tweets:");
        ProgressBar progress = new ProgressBar(FileUtils.countLines("res/tweets/10k.txt"));
        BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/10k.txt")));
        String line = "";
        int counter = 0;
        while((line = reader.readLine()) != null) {
            progress.printProgress(counter);
            line = filter(line);
            tweets.add(line);
            counter++;
        }
    }


//    Creation of phrase-vector from set of tweets containing phrase. Needs some cleanup. The phrase-vector should contain the x = phraseVectorSize most frequent words used together with the phrase.
    private static void createFinalHashMap() throws IOException {
        JSONLineByLine<Map<String, Integer[]>> ngrams = new JSONLineByLine<>("res/tweets/ngrams.txt", new TypeToken<Map<String, Integer[]>>(){}.getType());
        while(ngrams.hasNext()) {
            Map.Entry<String, Integer[]> entry = ngrams.next().entrySet().iterator().next();
            HashMap<String, Integer> [] wordFrequencies = new HashMap[]{new HashMap(), new HashMap()};
            for(int i = 0; i < entry.getValue().length; i++) {
                String tweet = tweets.get(entry.getValue()[i]);
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
    private static String[] constructPhraseWindows(String tweet, String key) {
        String[] tweetParts = punctuation.split(tweet);
        String[] keyWords = key.split(" ");
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

    private static boolean matchesAtIndex(String[] search, String[] needle, int index) {
        for(int j = 0 ; j < needle.length && index+j < search.length; j++) {
            if(!search[index+j].equals(needle[j])) {
                return false;
            }
        }
        return true;
    }

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
//        System.out.println("Printing similarities....");
//        for(Node node : nodes) {
//                for (Edge edge : node.getNeighbors()) {
//                    if(!node.getPhrase().contains(edge.getNeighbor().getPhrase()) && !edge.getNeighbor().getPhrase().contains(node.getPhrase()))
//                        System.out.println(node.getPhrase() + " and " + edge.getNeighbor().getPhrase() + "\n" + "Similarity: " + edge.getWeight() + "'\n");
//                }
//        }
    }

    private static String filter(String text) {
        text = Filters.HTMLUnescape(text);
        text = Filters.removeUnicodeEmoticons(text);
        text = Filters.normalizeForm(text);
        text = Filters.removeURL(text);
        text = Filters.removeRTTag(text);
        text = Filters.removeHashtag(text);
        text = Filters.removeUsername(text);
        text = Filters.removeEmoticons(text);
        text = Filters.removeInnerWordCharacters(text);
        text = Filters.removeNonSyntacticalTextPlus(text);
        text = Filters.removeFreeDigits(text);
        text = Filters.removeRepeatedWhitespace(text);
        return text.trim().toLowerCase();
    }
}
