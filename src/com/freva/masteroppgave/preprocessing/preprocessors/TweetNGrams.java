package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.utils.reader.LineReader;
import com.freva.masteroppgave.utils.tools.Parallel;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.NGrams;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;


public class TweetNGrams implements Progressable {
    private LineReader tweetReader;

    /**
     * Finds all frequent n-grams in a file, treating each new line as a new document.
     * @param input File with documents to generate n-grams for
     * @param n Maximum n-gram length
     * @param frequencyCutoff Smallest required frequency to include n-gram
     * @param filters List of TWEET_FILTERS to apply to document before generating n-grams
     * @return Map of n-grams as key and number of occurrences as value
     * @throws IOException
     */
    public final Map<String, Integer> getFrequentNGrams(File input, int n, double frequencyCutoff, List<Function<String, String>> filters) throws IOException {
        final AtomicInteger lineCounter = new AtomicInteger(0);
        tweetReader = new LineReader(input);
        NGramTree tree = new NGramTree();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");

        Parallel.For(tweetReader, tweet -> {
            tweet = Filters.stringChain(tweet, filters);
            synchronized (lineCounter) {
                if (lineCounter.incrementAndGet() % 50000 == 0) {
                    tree.pruneInfrequent((int) (frequencyCutoff * lineCounter.intValue()) / 2);
                }
            }

            for(String[] nGramTokens: NGrams.getSyntacticalNGrams(tweet, n)) {
                String nGram = StringUtils.join(nGramTokens, " ");
                if(! containsAlphabet.matcher(nGram).find()) continue;
                if(WordFilters.containsIntensifier(nGramTokens)) continue;
                if(WordFilters.isStopWord(nGramTokens[nGramTokens.length - 1])) continue;

                tree.incrementNGram(nGramTokens);
            }
        });

        return tree.getNGrams((int) (frequencyCutoff*lineCounter.intValue()));
    }


    @Override
    public double getProgress() {
        return tweetReader != null ? tweetReader.getProgress() : 0;
    }


    private class NGramTree {
        private Node root = new Node("");

        private synchronized void incrementNGram(String[] nGram) {
            Node current = root;
            for(String word: nGram) {
                if(! current.hasChild(word)) {
                    current.addChild(word);
                }

                current = current.getChild(word);
            }
            current.numOccurrences++;
        }

        private Node getNode(String[] nGram) {
            Node current = root;
            for(String word: nGram) {
                if(! current.hasChild(word)) {
                    return null;
                }

                current = current.getChild(word);
            }
            return current;
        }

        private void pruneTree(Node startNode){
            startNode.visited = true;
            for(Node child : startNode.children.values()){
                if(!child.visited){
                    pruneTree(child);
                }
            }

            if(startNode != root && !root.hasChild(startNode)) {
                for (String child : startNode.children.keySet()) {
                    Node childNode = startNode.children.get(child);
                    for (String phrase : childNode.phrases.keySet()) {
                        String combinedPhrase = startNode.phrase + " " + phrase;
                        Node nodeToAdjust = getNode(RegexFilters.WHITESPACE.split(combinedPhrase));
                        if(nodeToAdjust != null) {
                            nodeToAdjust.subtractNumOccurrences(childNode.numOccurrences);
                            startNode.phrases.put(combinedPhrase, childNode.getPhraseOccurrences(phrase));
                        }
                    }
                }

                Node currentNode = getNode(RegexFilters.WHITESPACE.split(startNode.phrase));
                if(currentNode != null){
                    currentNode.subtractNumOccurrences(startNode.numOccurrences);
                    startNode.phrases.put(startNode.phrase, startNode.numOccurrences);
                }
            }
        }

        private void pruneInfrequent(int limit) {
            root.pruneInfrequent(limit);
        }

        private Map<String, Integer> getNGrams(int limit) {
            Map<String, Integer> nGrams = new HashMap<>();
            root.pruneInfrequent(limit);
            pruneTree(root);
            root.addFrequentPhrases(nGrams, limit, "");
            return nGrams;
        }
    }


    private class Node {
        private Map<String, Node> children = new HashMap<>();
        private int numOccurrences;
        private boolean visited;
        private String phrase;
        private HashMap<String, Integer> phrases = new HashMap<>();

        public Node(String phrase){
            this.phrase = phrase;
        }

        public boolean hasChild(String value) {
            return children.containsKey(value);
        }

        public boolean hasChild(Node node){
            for(Node child : children.values()){
                if(child == node){
                    return true;
                }
            }
            return false;
        }

        public void addChild(String value) {
            children.put(value, new Node(value));
        }

        public Node getChild(String value) {
            return children.get(value);
        }

        public int getPhraseOccurrences(String phrase){
            return phrases.get(phrase);
        }


        public void subtractNumOccurrences(int toSubtract) {
            numOccurrences -= toSubtract;
        }

        private void pruneInfrequent(int limit) {
            Iterator<Map.Entry<String, Node>> iterator = children.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Node> child = iterator.next();
                child.getValue().pruneInfrequent(limit);

                if (child.getValue().numOccurrences < limit && child.getValue().children.size() == 0) {
                    iterator.remove();
                }
            }
        }



        private void addFrequentPhrases(Map<String, Integer> map, int limit, String prefix) {
            for(Map.Entry<String, Node> child: children.entrySet()) {
                int numChildOccurrences = child.getValue().numOccurrences - child.getValue().children.values().stream()
                        .filter(i -> i.numOccurrences >= limit).mapToInt(i -> i.numOccurrences).sum();
                if(numChildOccurrences >= limit) {
                    map.put(prefix + child.getKey(), numChildOccurrences);
                }

                child.getValue().addFrequentPhrases(map, limit, prefix + child.getKey() + " ");
            }
        }
    }
}