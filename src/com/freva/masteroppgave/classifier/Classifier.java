package com.freva.masteroppgave.classifier;


import com.freva.masteroppgave.classifier.sentence.LexicalParser;
import com.freva.masteroppgave.classifier.sentence.LexicalToken;
import com.freva.masteroppgave.lexicon.container.PhraseTree;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class Classifier {
    private Function<String, String>[] filters;
    private PriorPolarityLexicon lexicon;
    private PhraseTree phraseTree;

    private static final double neutralThreshold = 1.5;


    @SafeVarargs
    public Classifier(PriorPolarityLexicon lexicon, Function<String, String>... filters) throws IOException {
        this.lexicon = lexicon;
        this.filters = filters;
        this.phraseTree = new PhraseTree(lexicon.getSubjectiveWords());
    }


    public DataSetEntry.Class classify(String tweet) {
        tweet = Filters.chain(tweet, filters);
        List<LexicalToken> lexicalTokens = LexicalParser.lexicallyParseTweet(tweet, phraseTree);

        analyseTokens(lexicalTokens);
        double tweetSentimentScore = lexicalTokens.stream().mapToDouble(LexicalToken::getSentimentValue).sum();

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

            if(token.isInLexicon()) {
                token.setLexicalValue(lexicon.getPolarity(phrase));
            }

            if(WordFilters.isStopWord(phrase)) {
                propagateNegation(lexicalTokens, i);
            }

            if(WordFilters.isIntensifier(phrase)) {
                intensifyNext(lexicalTokens, i, WordFilters.getIntensifierValue(phrase));
            }
        }
    }

    private void propagateNegation(List<LexicalToken> lexicalTokens, int index) {
        for(int i = index + 1; i <= index + 4 && i < lexicalTokens.size(); i++) {
            lexicalTokens.get(i).setInNegatedContext(true);
            if(lexicalTokens.get(i).isAtTheEndOfSentence()) {
                break;
            }
        }
    }

    private void intensifyNext(List<LexicalToken> lexicalTokens, int index, double intensification) {
        if(! lexicalTokens.get(index).isAtTheEndOfSentence()){
            lexicalTokens.get(index+1).setIntensification(intensification);
        }
    }
}
