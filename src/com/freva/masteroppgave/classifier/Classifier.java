package com.freva.masteroppgave.classifier;


import com.freva.masteroppgave.classifier.sentence.LexicalToken;
import com.freva.masteroppgave.lexicon.container.PhraseTree;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;

import java.util.List;
import java.util.function.Function;

public class Classifier {
    private Function<String, String>[] filters;
    private PriorPolarityLexicon lexicon;
    private PhraseTree phraseTree;


    private final double neutralThreshold = 0.75;

    @SafeVarargs
    public Classifier(PriorPolarityLexicon lexicon, Function<String, String>... filters) {
        this.lexicon = lexicon;
        this.filters = filters;
        this.phraseTree = new PhraseTree(lexicon.getSubjectiveWords());
    }

    public DataSetEntry.Class classify(String tweet) {
        tweet = Filters.chain(tweet, filters);
        List<LexicalToken> lexicalTokens = null;
        analyseTokens(lexicalTokens);
        double tweetSentimentScore = 0;
        for (LexicalToken lexicalToken : lexicalTokens) {
            tweetSentimentScore += lexicalToken.getSentimentValue();
        }
        if (Math.abs(tweetSentimentScore) > neutralThreshold) {
            if (tweetSentimentScore > 0) {
                return DataSetEntry.Class.POSITIVE;
            }
            return DataSetEntry.Class.NEGATIVE;
        }
        return DataSetEntry.Class.NEUTRAL;
    }

    private void analyseTokens(List<LexicalToken> lexicalTokens) {
        for(int i = 0; i < lexicalTokens.size(); i++) {
            LexicalToken token = lexicalTokens.get(i);
            String phrase = token.getPhrase();
            token.setLexicalValue(lexicon.getPolarity(phrase));
            if(WordFilters.isNegator(phrase)) {
                propagateNegation(lexicalTokens, i);
            }
            if(WordFilters.isIntensifier(phrase)) {
                intensifyNext(lexicalTokens, i);
            }
        }
    }

    private void propagateNegation(List<LexicalToken> lexicalTokens, int index) {
        for(int i = index + 1; i <= index + 4; i++) {
            lexicalTokens.get(i).setInNegatedContext(true);
            if(lexicalTokens.get(i).isAtTheEndOfSentence()) {
                break;
            }
        }
    }

    private void intensifyNext(List<LexicalToken> lexicalTokens, int index) {
        if(!lexicalTokens.get(index).isAtTheEndOfSentence()){
            lexicalTokens.get(index+1).setUnderIntensification(true);
        }
    }
}
