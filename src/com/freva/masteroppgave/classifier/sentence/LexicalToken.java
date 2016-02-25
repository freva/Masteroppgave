package com.freva.masteroppgave.classifier.sentence;

public class LexicalToken {

    private int lexicalValue;
    private boolean inNegatedContext;
    private boolean atEndOfSentence;
    private boolean underIntensification;

    public LexicalToken(int lexicalValue) {
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
