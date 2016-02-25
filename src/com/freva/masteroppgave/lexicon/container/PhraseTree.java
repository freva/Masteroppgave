package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;

import java.util.*;
import java.util.List;

public class PhraseTree {
    private Node root = new Node();

    /**
     * Creates a phrase tree for efficient sub-phrase look up.
     * @param phrases Collection of Strings of all the phrases which are whitespace delimited n-grams
     */
    public PhraseTree(Collection<String> phrases) {
        for(String phrase: phrases) {
            String[] tokens = RegexFilters.WHITESPACE.split(phrase);

            Node tree = root;
            for(String token: tokens) {
                if(! tree.hasChild(token)) {
                    tree.addChild(token);
                }
                tree = tree.getChild(token);
            }
            tree.setPhraseEnd(true);
        }
    }


    /**
     * Checks if phrase or sub-phrase exists in the tree. If set of phrases contains phrases such as: "state", "of the"
     * and "state of the art", look up on:
     * "state" returns true, "of" returns null, "of the art" returns false.
     * @param phrase Phrase or sub-phrase to look up.
     * @return Returns true if phrase in its entirety is in the tree, null if part of the phrase matches a larger phrase,
     * false if phrases matches no phrase entirely or any longer phrase.
     */
    public Boolean hasPhrase(String... phrase) {
        Node tree = root;
        for(String token: phrase) {
            if(! tree.hasChild(token)) return false;
            tree = tree.getChild(token);
        }

        return tree.isEndOfPhrase() ? true : null;
    }


    /**
     * Finds word-ranges all of phrases in tokens stored in PhraseTree
     * @param sentence Sentence to search for phrases
     * @return Map of Points, where (x, y) coordinates denote start and end (inclusive) index in tokens of phrase, and
     * String which stores the actual phrase found
     */
    public List<Phrase> findTrackedWords(String sentence) {
        String[] tokens = RegexFilters.WHITESPACE.split(sentence);
        return findTrackedWords(tokens);
    }


    /**
     * Finds word-ranges all of phrases in tokens stored in PhraseTree
     * @param tokens Sequence of tokens to find phrases in
     * @return Map of Points, where (x, y) coordinates denote start and end (inclusive) index in tokens of phrase, and
     * String which stores the actual phrase found
     */
    public List<Phrase> findTrackedWords(String[] tokens) {
        List<Phrase> trackedWords = new ArrayList<>();

        for(int i=0; i<tokens.length; i++) {
            for(int j=i+1; j<=tokens.length; j++) {
                String[] phrase = Arrays.copyOfRange(tokens, i, j);
                Boolean status = hasPhrase(phrase);

                if(status == null) {
                    continue;
                } if(status.equals(true)) {
                    trackedWords.add(new Phrase(String.join(" ", phrase), i, j-1));
                } else if(status.equals(false)) {
                    break;
                }
            }
        }
        return trackedWords;
    }


    public class Phrase implements Comparable<Phrase> {
        private final int startIndex, endIndex;
        private final String phrase;

        public Phrase(String phrase, int startIndex, int endIndex) {
            this.phrase = phrase;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getPhrase() {
            return phrase;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getPhraseLength() {
            return endIndex-startIndex;
        }

        /**
         * Checks if two phrases overlap
         * @param other The other phrase
         * @return true if overlap, false otherwise
         */
        public boolean overlapsWith(Phrase other) {
            return this.getStartIndex() <= other.getEndIndex() && other.getStartIndex() <= this.getEndIndex();
        }

        @Override
        public int compareTo(Phrase other) {
            int sizeDiff = other.getPhraseLength() - this.getPhraseLength();
            return sizeDiff != 0 ? sizeDiff : other.getStartIndex() - this.getStartIndex();
        }

        public String toString() {
            return "(" + phrase + ", " + startIndex + ", " + endIndex + ")";
        }
    }


    private class Node {
        private Map<String, Node> children = new HashMap<>();
        private boolean endOfPhrase = false;

        public boolean hasChild(String value) {
            return children.containsKey(value);
        }

        public void addChild(String value) {
            children.put(value, new Node());
        }

        public Node getChild(String value) {
            return children.get(value);
        }

        public void setPhraseEnd(boolean phraseEnd) {
            endOfPhrase = phraseEnd;
        }

        public boolean isEndOfPhrase() {
            return endOfPhrase;
        }
    }
}
