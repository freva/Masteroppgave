package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;

import java.util.*;

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
