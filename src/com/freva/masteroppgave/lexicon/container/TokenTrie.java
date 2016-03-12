package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;

import java.util.*;
import java.util.List;

public class TokenTrie {
    private Node root = new Node();

    public TokenTrie() {}

    /**
     * Creates a tokenSequence tree for efficient sub-tokenSequence look up.
     * @param sentences Collection of Strings of all the phrases which are whitespace delimited n-grams
     */
    public TokenTrie(Collection<String> sentences) {
        this();
        for(String sentence: sentences) {
            String[] words = RegexFilters.WHITESPACE.split(sentence);
            addTokenSequence(words);
        }
    }


    public void addTokenSequence(String[] tokenSequence) {
        Node tree = root;
        for(String token: tokenSequence) {
            if(! tree.hasChild(token)) {
                tree.addChild(token);
            }
            tree = tree.getChild(token);
        }
        tree.setPhraseEnd(true);
    }





    /**
     * Checks if tokenSequence or sub-tokenSequence exists in the tree. If set of phrases contains phrases such as: "state", "of the"
     * and "state of the art", look up on:
     * "state" returns true, "of" returns null, "of the art" returns false.
     * @param phrase Token or sub-tokenSequence to look up.
     * @return Returns true if tokenSequence in its entirety is in the tree, null if part of the tokenSequence matches a larger tokenSequence,
     * false if phrases matches no tokenSequence entirely or any longer tokenSequence.
     */
    public Boolean hasTokens(String[] phrase) {
        if(phrase.length == 1 && WordFilters.isSpecialClassWord(phrase[0])) return true;

        Node tree = root;
        for(String token: phrase) {
            if(! tree.hasChild(token)) return false;
            tree = tree.getChild(token);
        }

        return tree.isEndOfPhrase() ? true : null;
    }


    /**
     * Finds word-ranges all of phrases in tokens stored in TokenTrie
     * @param tokens Sequence of tokens to find phrases in
     * @return Map of Points, where (x, y) coordinates denote start and end (inclusive) index in tokens of tokenSequence, and
     * String which stores the actual tokenSequence found
     */
    public List<Token> findTrackedWords(String[] tokens) {
        List<Token> trackedWords = new ArrayList<>();

        for(int i=0; i<tokens.length; i++) {
            for(int j=i+1; j<=tokens.length; j++) {
                String[] phrase = Arrays.copyOfRange(tokens, i, j);
                Boolean status = hasTokens(phrase);

                if(status == null) {
                    continue;
                } if(status.equals(true)) {
                    trackedWords.add(new Token(phrase, i, j-1));
                } else if(status.equals(false)) {
                    break;
                }
            }
        }
        return trackedWords;
    }


    public List<Token> findOptimalAllocation(String[] tokens) {
        List<Token> tokenRanges = findTrackedWords(tokens);
        Collections.sort(tokenRanges);

        for(int offset = 1; offset< tokenRanges.size(); offset++) {
            Iterator<Token> iter = tokenRanges.listIterator(offset);

            while(iter.hasNext()) {
                Token candidate = iter.next();
                for(int i=0; i<offset; i++) {
                    if(tokenRanges.get(i).overlapsWith(candidate)) {
                        iter.remove();
                        break;
                    }
                }
            }
        }

        Collections.sort(tokenRanges, ((o1, o2) -> o1.getStartIndex()-o2.getStartIndex()));
        return tokenRanges;
    }


    public class Token implements Comparable<Token> {
        private final int startIndex, endIndex;
        private final String[] tokenSequence;

        public Token(String[] tokenSequence, int startIndex, int endIndex) {
            this.tokenSequence = tokenSequence;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String[] getTokenSequence() {
            return tokenSequence;
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
         * @param other The other tokenSequence
         * @return true if overlap, false otherwise
         */
        public boolean overlapsWith(Token other) {
            return this.getStartIndex() <= other.getEndIndex() && other.getStartIndex() <= this.getEndIndex();
        }

        @Override
        public int compareTo(Token other) {
            int sizeDiff = other.getPhraseLength() - this.getPhraseLength();
            return sizeDiff != 0 ? sizeDiff : other.getStartIndex() - this.getStartIndex();
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
