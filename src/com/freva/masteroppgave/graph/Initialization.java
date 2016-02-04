package com.freva.masteroppgave.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Initialization {
    private HashMap<String, int[]> phraseOccurrences = new HashMap<>();
    private ArrayList<String> tweets = new ArrayList<>();
    private HashMap<String, String[]> phraseInTweets = new HashMap<>();
    private HashMap<String, Integer> priorPolarityLexicon = new HashMap<>();

    private static final Pattern posTagPattern = Pattern.compile("_([A-Z$]*)\\s");
    private static final int phraseFrequencyThreshold = 25;
    private static final int phraseVectorSize = 20;


    public Initialization() throws IOException{
        System.out.println("Dictionary to HashMap...");
        dictToHashmap();
        System.out.println("Reading tweets...");
        readTweets();
        readPriorPolarityLexicon();
        System.out.println("Creating final HashMap...");
        createFinalHashMap();
        System.out.println("Creating graph...");
        createGraph();
    }

    private void dictToHashmap() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader (new File("res/tweets/phraseDict.txt")));
        String[] entries = reader.readLine().replace("}", "").split("]");
        for (String entry : entries) {
            String[] parts = entry.split("\\[");
            String key = parts[0].split("\"")[1];
            String[] textValues = parts[1].split(", ");
            int[] values = new int[textValues.length];
            for (int i = 0; i < textValues.length; i++) {
                values[i] = Integer.valueOf(textValues[i]);
            }
            phraseOccurrences.put(key, values);
        }
    }

    private void readTweets() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/tagged.txt")));
        String line = "";
        while((line = reader.readLine()) != null) {
            String newLine = posTagPattern.matcher(line).replaceAll(" ");
            newLine = newLine.replaceAll("'", "");
            newLine = newLine.replaceAll("[^A-Za-z]", " ");
            newLine = newLine.replaceAll("\\s+", " ");
            tweets.add(newLine.substring(0, newLine.length()-3).trim().toLowerCase());
        }
    }

    private void readPriorPolarityLexicon() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/AFINN111.txt")));
        String line = "";
        while ((line = reader.readLine()) != null) {
            String[] wordAndScore = line.split("\\s+");
            String key = "";
            for(int i = 0; i < wordAndScore.length-1; i++) {
                key += wordAndScore[i] + " ";
            }
            priorPolarityLexicon.put(key.trim(), Integer.valueOf(wordAndScore[wordAndScore.length-1]));
        }
    }

//    Creation of phrase-vector from set of tweets containing phrase. Needs some cleanup. The phrase-vector should contain the x = phraseVectorSize most frequent words used together with the phrase.
    private void createFinalHashMap() {
        for(String key : phraseOccurrences.keySet()) {
            int[] tweetIDs = phraseOccurrences.get(key);;
            if (tweetIDs.length > phraseFrequencyThreshold) {
                String[] relatedWords = new String[phraseVectorSize];
                HashMap<String, Integer> wordFrequency = new HashMap<>();
                for (int tweetID : tweetIDs) {
                    String phraseWindow = constructPhraseWindow(tweets.get(tweetID), key);
                    for (String word : phraseWindow.split(" ")) {
                        int count = wordFrequency.containsKey(word) ? wordFrequency.get(word) : 0;
                        wordFrequency.put(word, count + 1);
                    }
                }
                HashMap<String, Integer> sortedWordFrequency = sortByValues(wordFrequency);
                int counter = 0;
                for (String sortedKey : sortedWordFrequency.keySet()) {
                    if (counter < phraseVectorSize) {
                        relatedWords[counter] = sortedKey;
                    } else {
                        break;
                    }
                    counter++;
                }
                phraseInTweets.put(key, relatedWords);
            }
        }
    }

//    Finds where the phrase starts and then creates a String phraseWindow containing the 2 words in front of the phrase, the phrase, and then the 2 following words. Ugly code needs cleanup
    private String constructPhraseWindow(String tweet, String key) {
        String phraseWindow = "";
        String[] wordsInTweet = tweet.split(" ");
        String[] keyWords = key.split(" ");
        int index = 0;
        for(int i = 0; i < wordsInTweet.length; i++) {
            if(keyWords.length > 1 && i != wordsInTweet.length-1) {
                if (wordsInTweet[i].equalsIgnoreCase(keyWords[0]) && wordsInTweet[i + 1].equalsIgnoreCase(keyWords[1])) {
                    index = i;
                    break;
                }
            else if (wordsInTweet[i].equalsIgnoreCase(keyWords[0])) {
                index = i;
                break;
                }
            }
        }
        for(int j = index-2; j <= index+3; j++) {
            if(j >= 0 && j < wordsInTweet.length) {
                phraseWindow += wordsInTweet[j] + " ";
            }
        }
        return phraseWindow.trim();

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
            for(Edge edge : node.getNeighbors()) {
                System.out.println(node.getPhrase() + " and " + edge.getNeighbor().getPhrase() + "\n" + "Similarity: " + edge.getWeight() + "'\n");
            }
        }
    }


    public static void main(String args[]) throws IOException{
        new Initialization();
    }
}
