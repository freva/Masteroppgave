package com.freva.masteroppgave.classifier.sentence;

public class LexicalToken {

    private final float intensificationValue = 1.5f;
    private final float negationValue = -0.75f;

    private int lexicalValue;
    private String phrase;
    private boolean inLexicon = false;
    private boolean inNegatedContext;
    private boolean atEndOfSentence;
    private boolean underIntensification;

    public LexicalToken(String phrase, boolean inLexicon) {
        this.phrase = phrase;
        this.inLexicon = inLexicon;
    }


    public String getPhrase() {
        return phrase;
    }

    public void setLexicalValue(int lexicalValue) {
        this.lexicalValue = lexicalValue;
    }

    public int getLexicalValue() {
        return lexicalValue;
    }

    public void setInNegatedContext(boolean inNegatedContext) {
        this.inNegatedContext = inNegatedContext;
    }

    public void setAtEndOfSentence(boolean atEndOfSentence) {
        this.atEndOfSentence = atEndOfSentence;
    }

    public void setUnderIntensification(boolean underIntensification) {
        this.underIntensification = underIntensification;
    }

    public double getSentimentValue() {
        double sentimentValue = lexicalValue;
        if(underIntensification) {
            sentimentValue *= intensificationValue;
        }
        if(inNegatedContext) {
            sentimentValue *= negationValue;
        }
        return sentimentValue;
    }

    public boolean isInNegatedContext() {
        return inNegatedContext;
    }

    public boolean isAtTheEndOfSentence() {
        return atEndOfSentence;
    }

    public boolean isUnderIntensification() {
        return underIntensification;
    }
}
