package com.freva.masteroppgave.classifier;

import com.freva.masteroppgave.classifier.sentence.LexicalParser;
import com.freva.masteroppgave.classifier.sentence.LexicalToken;
import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;

import java.util.List;


public class Classifier {
    private PriorPolarityLexicon lexicon;
    private TokenTrie phraseTree;
    private Filters filters;

    public Classifier(PriorPolarityLexicon lexicon, Filters filters) {
        this.lexicon = lexicon;
        this.filters = filters;
        this.phraseTree = new TokenTrie(lexicon.getSubjectiveWords());
    }

    public Classifier(PriorPolarityLexicon lexicon) {
        this(lexicon, null);
    }


    public double calculateSentiment(String tweet) {
        if (filters != null) {
            tweet = filters.apply(tweet);
        }

        List<LexicalToken> lexicalTokens = LexicalParser.lexicallyParseTweet(tweet, phraseTree);
        analyseTokens(lexicalTokens);

        return lexicalTokens.stream().mapToDouble(LexicalToken::getSentimentValue).sum();
    }

    private void analyseTokens(List<LexicalToken> lexicalTokens) {
        for(int i = 0; i < lexicalTokens.size(); i++) {
            LexicalToken token = lexicalTokens.get(i);
            String phrase = token.getPhrase();

            if(lexicon.hasWord(phrase)) {
                token.setLexicalValue(lexicon.getPolarity(phrase));

            } else if(ClassifierOptions.isNegation(phrase)) {
                propagateNegation(lexicalTokens, i);

            } else if(ClassifierOptions.isIntensifier(phrase)) {
                intensifyNext(lexicalTokens, i, ClassifierOptions.getIntensifierValue(phrase));
            }
        }
    }

    private void propagateNegation(List<LexicalToken> lexicalTokens, int index) {
        final double negationScopeLength = ClassifierOptions.getVariable(ClassifierOptions.Variable.NEGATION_SCOPE_LENGTH);
        for(int i = index + 1; i <= index + negationScopeLength && i < lexicalTokens.size(); i++) {
            lexicalTokens.get(i).setInNegatedContext(true);
            if(lexicalTokens.get(i).isAtTheEndOfSentence()) {
                break;
            }
        }
    }

    private void intensifyNext(List<LexicalToken> lexicalTokens, int index, double intensification) {
        intensification *= ClassifierOptions.getVariable(intensification > 1 ? ClassifierOptions.Variable.AMPLIFIER_SCALAR : ClassifierOptions.Variable.DOWNTONER_SCALAR);
        if (! lexicalTokens.get(index).isAtTheEndOfSentence()) {
            lexicalTokens.get(index + 1).intensifyToken(intensification);
        }
    }
}
