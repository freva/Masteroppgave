package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.reader.TweetReader;
import com.freva.masteroppgave.utils.tools.NGrams;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;


public class TweetNGrams implements Progressable {
    private TweetReader tweetReader;

    /**
     * Finds all frequent n-grams in a file, treating each new line as a new document.
     * @param input File with documents to generate n-grams for
     * @param n Maximum n-gram length
     * @param frequencyCutoff Smallest required frequency to include n-gram
     * @param filters List of filters to apply to document before generating n-grams
     * @return Map of n-grams as key and number of occurrences as value
     * @throws IOException
     */
    public final Map<String, Integer> getFrequentNGrams(File input, int n, double frequencyCutoff, List<Function<String, String>> filters) throws IOException {
        this.tweetReader = new TweetReader(input, filters);
        NGramTree tree = new NGramTree();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");
        int lineCounter = 0;

        for(String line: tweetReader) {
            if(lineCounter % 50000 == 0 && lineCounter++ != 0) {
                tree.pruneInfrequent((int) (frequencyCutoff * lineCounter) / 2);
            }

            for(String[] nGramTokens: NGrams.getSyntacticalNGrams(line, n)) {
                String nGram = StringUtils.join(nGramTokens, " ");
                if(! containsAlphabet.matcher(nGram).find()) continue;
                if(WordFilters.containsIntensifier(nGramTokens) || WordFilters.containsNegation(nGramTokens)) continue;
                if(WordFilters.isStopWord(nGramTokens[0]) || WordFilters.isStopWord(nGramTokens[nGramTokens.length - 1])) continue;

                tree.incrementNGram(nGramTokens);
            }
        }

        return tree.getNGrams((int) (frequencyCutoff*lineCounter));
    }


    @Override
    public double getProgress() {
        return tweetReader != null ? tweetReader.getProgress() : 0;
    }


    private class NGramTree {
        private Node root = new Node();

        private void incrementNGram(String[] nGram) {
            Node current = root;
            for(String word: nGram) {
                if(! current.hasChild(word)) {
                    current.addChild(word);
                }

                current = current.getChild(word);
            }
            current.numOccurrences++;
        }

        private void pruneInfrequent(int limit) {
            root.pruneInfrequent(limit);
        }

        private Map<String, Integer> getNGrams(int limit) {
            Map<String, Integer> nGrams = new HashMap<>();
            root.pruneInfrequent(limit);
            root.addFrequentPhrases(nGrams, limit, "");
            return nGrams;
        }
    }


    private class Node {
        private Map<String, Node> children = new HashMap<>();
        private int numOccurrences;

        public boolean hasChild(String value) {
            return children.containsKey(value);
        }

        public void addChild(String value) {
            children.put(value, new Node());
        }

        public Node getChild(String value) {
            return children.get(value);
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