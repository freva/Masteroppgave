package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Edge;
import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.graph.Node;
import com.freva.masteroppgave.lexicon.utils.PhraseCreator;
import com.freva.masteroppgave.lexicon.utils.PolarityWordsDetector;
import com.freva.masteroppgave.preprocessing.filters.FilterStopWords;
import com.freva.masteroppgave.preprocessing.filters.Filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Initialization {
    private PhraseCreator phraseCreator = new PhraseCreator();
    private PolarityWordsDetector polarityWordsDetector;
    private HashMap<String, ArrayList<Integer>> phraseOccurrences = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> polarityWordOccurences = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> wordAndPhraseOccurences = new HashMap<>();
    private ArrayList<String> tweets = new ArrayList<>();
    private HashMap<String, String[]> phraseInTweets = new HashMap<>();
    private HashMap<String, Integer> polarityLexicon = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> wordsAndPhrases = new HashMap<>();

    private static final int phraseFrequencyThreshold = 25;
    private static final int phraseVectorSize = 8;


    public Initialization() throws IOException{
        readPolarityLexicon();
        readNGrams();
        mergePolarityAndNGrams();
        readTweets();
        createFinalHashMap();
        createGraph();
    }


    private void readTweets() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/10k.txt")));
        String line = "";
        while((line = reader.readLine()) != null) {
//            phraseCreator.detectPhrases(line);
//
//            String newLine = Filters.removePosTags(line).toLowerCase();
//            polarityWordsDetector.detectPolarityWords(newLine);
            tweets.add(line);
        }
//        phraseOccurrences = phraseCreator.getPhrases();
//        polarityWordOccurences = polarityWordsDetector.getWordOccurences();
    }


    private void readPolarityLexicon() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/AFINN111.txt")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] wordAndScore = line.split("\\s+");
                String key = "";
                for (int i = 0; i < wordAndScore.length - 1; i++) {
                    key += wordAndScore[i] + " ";
                }
                polarityLexicon.put(key.trim().toLowerCase(), Integer.valueOf(wordAndScore[wordAndScore.length - 1]));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        polarityWordsDetector = new PolarityWordsDetector(polarityLexicon);
    }


    private void readNGrams() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/ngrams.txt")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String key = line.split(":")[0];
                wordsAndPhrases.put(key.trim().toLowerCase(), new HashMap<>());
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void mergePolarityAndNGrams() {
        for(String polarityWord : polarityLexicon.keySet()) {
            if(!wordsAndPhrases.containsKey(polarityWord)) {
                wordsAndPhrases.put(polarityWord, new HashMap<>());
            }
        }
    }



//    Creation of phrase-vector from set of tweets containing phrase. Needs some cleanup. The phrase-vector should contain the x = phraseVectorSize most frequent words used together with the phrase.
    private void createFinalHashMap() {
        int phraseNr = 0;
        for(String phrase : wordsAndPhrases.keySet()) {
            System.out.print("\r " +phraseNr++);
            HashMap<String, Integer> wordFrequency = wordsAndPhrases.get(phrase);
            for(String tweet : tweets) {
                String filteredTweet = FilterStopWords.replaceStopWords(tweet, "_");
                if(filteredTweet.contains(phrase)) {
                    String phraseWindow = constructPhraseWindow(filteredTweet, phrase);
                    for(String word : phraseWindow.split(" ")) {
                        int count = wordFrequency.containsKey(word) ? wordFrequency.get(word) : 0;
                        wordFrequency.put(word, count + 1);
                    }
                }
            }
            HashMap<String, Integer> sortedWordFrequency = sortByValues(wordFrequency);
            int counter = 0;
            String[] relatedWords = new String[phraseVectorSize];
            for (String sortedKey : sortedWordFrequency.keySet()) {
                if (counter < phraseVectorSize) {
                    relatedWords[counter] = sortedKey;
                } else {
                    break;
                }
                counter++;
            }
            phraseInTweets.put(phrase, relatedWords);
        }
    }


    //Finds where the phrase starts and then creates a String phraseWindow containing the 2 words in front of the phrase,
    //the phrase, and then the 2 following words. Ugly code needs cleanup
    private String constructPhraseWindow(String filteredTweet, String key) {
        String phraseWindow = "";
        String[] wordsInTweet = filteredTweet.split(" ");
        String[] keyWords = key.split(" ");
        int index = 0;
        for(int i = 0; i < wordsInTweet.length; i++) {
            if(matchesAtIndex(wordsInTweet, keyWords, i)) {
                index = i;
                break;
            }
        }
        for(int j = index-2; j <= index+3; j++) {
            if(j >= 0 && j < wordsInTweet.length && !wordsInTweet[j].equals("_")) {
                phraseWindow += wordsInTweet[j] + " ";
            }
        }
        return phraseWindow.trim();

    }

    private boolean matchesAtIndex(String[] search, String[] needle, int index) {
        for(int j = 0 ; j < needle.length && index+j < search.length; j++) {
            if(!search[index+j].equals(needle[j])) {
                return false;
            }
        }
        return true;
    }

//    Directly from internet:
    private HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Object aList : list) {
            Map.Entry entry = (Map.Entry) aList;
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private void createGraph() {
        Graph graph = new Graph();
        System.out.println("Adding nodes...");
        for(String key : phraseInTweets.keySet()) {
            graph.addNode(new Node(key, phraseInTweets.get(key)));
        }
        System.out.println("Number of nodes: " + graph.getNodes().size());
        System.out.println("Creating and weighing edges...");
        graph.createAndWeighEdges();
        ArrayList<Node> nodes = graph.getNodes();
        System.out.println("Printing similarities....");
        for(Node node : nodes) {
            if(polarityLexicon.containsKey(node.getPhrase())) {
                for (Edge edge : node.getNeighbors()) {
                    System.out.println(node.getPhrase() + " and " + edge.getNeighbor().getPhrase() + "\n" + "Similarity: " + edge.getWeight() + "'\n");
                }
            }
        }
    }



    public static void main(String args[]) throws IOException{
        new Initialization();
    }
}
