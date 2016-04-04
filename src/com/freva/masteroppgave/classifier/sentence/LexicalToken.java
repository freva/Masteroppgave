package com.freva.masteroppgave.classifier.sentence;

import com.freva.masteroppgave.classifier.ClassifierOptions;

public class LexicalToken {
    private String phrase;
    private double lexicalValue;
    private double intensification = 1;

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

    public void intensifyToken(double intensification) {
        this.intensification *= intensification;
    }

    public double getSentimentValue() {
        double sentimentValue = lexicalValue;
        if (isUnderIntensification()) {
            sentimentValue *= intensification;
        }

        if (isInNegatedContext() && sentimentValue != 0) {
            final double negationValue = ClassifierOptions.getVariable(ClassifierOptions.Variable.NEGATION_VALUE);
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
        return intensification != 1;
    }

    public String toString() {
        return "[" + phrase + (isInNegatedContext() ? "_NEG" : "") + " | " + getSentimentValue() + " | " +
                intensification + "]";
    }
}
