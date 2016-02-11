package com.freva.masteroppgave.lexicon.graph;


public class ContextWords {
    private String[] leftSideContextWords;
    private String[] rightSideContextWords;

    public ContextWords(String[] leftSideContextWords, String[] rightSideContextWords) {
        this.leftSideContextWords = leftSideContextWords;
        this.rightSideContextWords = rightSideContextWords;
    }

    public String[] getLeftSideContextWords() {
        return leftSideContextWords;
    }

    public String[] getRightSideContextWords() {
        return rightSideContextWords;
    }
}
