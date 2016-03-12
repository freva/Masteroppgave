package com.freva.masteroppgave.classifier.sentence;

public class LexicalToken {
    private static final double negationValue = 2;

    private String phrase;
    private double lexicalValue;
    private double intensification;

    private boolean inNegatedContext;
    private boolean atEndOfSentence;


    public LexicalToken(String phrase) {
        this.phrase = phrase;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setLexicalValue(double lexicalValue) {
        this.lexicalValue = lexicalValue;
    }

    public double getLexicalValue() {
        return lexicalValue;
    }

    public void setInNegatedContext(boolean inNegatedContext) {
        this.inNegatedContext = inNegatedContext;
    }

    public void setAtEndOfSentence(boolean atEndOfSentence) {
        this.atEndOfSentence = atEndOfSentence;
    }

    public void setIntensification(double intensification) {
        this.intensification = intensification;
    }

    public double getSentimentValue() {
        double sentimentValue = lexicalValue;
        if(isUnderIntensification()) {
            sentimentValue *= intensification;
        }

        if(inNegatedContext && sentimentValue != 0) {
            sentimentValue = (sentimentValue > 0) ? sentimentValue - negationValue : sentimentValue + negationValue;
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
        return intensification != 0;
    }

    public String toString() {
        return "[" + phrase + (inNegatedContext ? "_NEG" : "") + " | " + getSentimentValue() + " | " +
                intensification + "]";
    }
}
