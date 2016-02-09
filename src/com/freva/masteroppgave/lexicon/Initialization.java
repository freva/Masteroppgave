package com.freva.masteroppgave.lexicon;

import com.freva.masteroppgave.lexicon.graph.Edge;
import com.freva.masteroppgave.lexicon.graph.Graph;
import com.freva.masteroppgave.lexicon.graph.Node;
import com.freva.masteroppgave.lexicon.utils.PolarityWordsDetector;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.JSONLineByLine;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.*;


public class Initialization {
    private PolarityWordsDetector polarityWordsDetector;
    private HashMap<String, ArrayList<Integer>> polarityWordOccurrences = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> wordAndPhraseOccurences = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> nGramOccurrences = new HashMap<>();
    private ArrayList<String> tweets = new ArrayList<>();
    private HashMap<String, String[][]> phraseInTweets = new HashMap<>();
    private HashMap<String, Integer> polarityLexicon = new HashMap<>();

    private static final int phraseVectorSize = 10;


    public Initialization() throws IOException, JSONException {
//        readPolarityLexicon();
        readTweets();
        System.out.println("Done reading tweets");
//        mergePolarityAndNGrams();
        createFinalHashMap();
        createGraph();
    }


    private void readTweets() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/200k.txt")));
        String line = "";
        int counter = 0;
        while((line = reader.readLine()) != null) {
            System.out.print("\r " + counter++);
            line = filter(line);
            tweets.add(line.toLowerCase());
        }
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
    }


    private void mergePolarityAndNGrams() {
        for(String nGram : nGramOccurrences.keySet()) {
            wordAndPhraseOccurences.put(nGram, nGramOccurrences.get(nGram));
        }
        for(String polarityWord : polarityWordOccurrences.keySet()) {
            if(polarityWordOccurrences.get(polarityWord).size() < 20) {
                continue;
            }
            if(!wordAndPhraseOccurences.containsKey(polarityWord)) {
                wordAndPhraseOccurences.put(polarityWord, polarityWordOccurrences.get(polarityWord));

            }
        }
    }



//    Creation of phrase-vector from set of tweets containing phrase. Needs some cleanup. The phrase-vector should contain the x = phraseVectorSize most frequent words used together with the phrase.
    private void createFinalHashMap() throws FileNotFoundException, JSONException {
        int phraseNr = 0;
        JSONLineByLine<String, JSONArray> ngrams = new JSONLineByLine<>("res/tweets/ngrams.txt");
        while(ngrams.hasNext()) {
            Map.Entry<String, JSONArray> entry = ngrams.next();
            System.out.print("\r " +phraseNr++);
            HashMap<String, Integer> wordFrequency = new HashMap<>();
            HashMap<String, Integer> [] wordFrequencies = new HashMap[]{new HashMap(), new HashMap()};
            for(int i = 0; i < entry.getValue().length(); i++) {
                String tweet = tweets.get(entry.getValue().getInt(i));
                if(tweet.contains(entry.getKey())) {
                    String[] phraseWindow = constructPhraseWindow(tweet, entry.getKey());
                    for(int j = 0; j < phraseWindow.length; j++) {
                        for(String word : phraseWindow[j].trim().split(" ")) {

                            int count = wordFrequencies[j].containsKey(word) ? wordFrequencies[j].get(word) : 0;
                            wordFrequencies[j].put(word, count + 1);
                        }
                    }
                }
            }
            String[][] phraseVectors = new String[2][phraseVectorSize];
            for(int i = 0; i < wordFrequencies.length; i++) {
                HashMap<String, Integer> sortedWordFrequency = sortByValues(wordFrequencies[i]);
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
    private String[] constructPhraseWindow(String filteredTweet, String key) {
        String phraseWindow = "";
        String[] wordsInTweet = filteredTweet.split(" ");
        String[] keyWords = key.split(" ");
        String[] phraseWindows = {"",""};
        ArrayList<Integer> indexes = new ArrayList<>();
//        int index = 0;
        for(int i = 0; i < wordsInTweet.length; i++) {
            if(matchesAtIndex(wordsInTweet, keyWords, i)) {
                indexes.add(i);
//                index = i;
//                break;
            }
        }
        for(int i = 0; i < indexes.size(); i++) {
            for (int j = indexes.get(i) - 2; j <= indexes.get(i) + (keyWords.length - 1) + 3; j++) {
                if (j >= 0 && j < wordsInTweet.length && !wordsInTweet[j].equals("_")) {
                    if (j <= indexes.get(i) + (keyWords.length - 1)) {
                        phraseWindows[0] += wordsInTweet[j] + " ";
                    }
                    if (j >= indexes.get(i)) {
                        phraseWindows[1] += wordsInTweet[j] + " ";
                    }
                    //                phraseWindow += wordsInTweet[j] + " ";
                }
            }
        }
        return phraseWindows;
//        return phraseWindow.trim();

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
                for (Edge edge : node.getNeighbors()) {
                    if(!node.getPhrase().contains(edge.getNeighbor().getPhrase()) && !edge.getNeighbor().getPhrase().contains(node.getPhrase()))
                        System.out.println(node.getPhrase() + " and " + edge.getNeighbor().getPhrase() + "\n" + "Similarity: " + edge.getWeight() + "'\n");
                }
        }
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
        text = Filters.removeNonAlphanumericalText(text);
        text = Filters.removeFreeDigits(text);
        text = Filters.removeRepeatedWhitespace(text);
        return text.trim();
    }



    public static void main(String args[]) throws Exception{
        new Initialization();
    }
}
