package com.freva.masteroppgave.classifier;

import com.freva.masteroppgave.classifier.sentence.LexicalParser;
import com.freva.masteroppgave.classifier.sentence.LexicalToken;
import com.freva.masteroppgave.lexicon.container.PhraseTree;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;


public class Classifier {
    private List<Function<String, String>> filters;
    private PriorPolarityLexicon lexicon;
    private PhraseTree phraseTree;


    public Classifier(PriorPolarityLexicon lexicon, List<Function<String, String>> filters) throws IOException {
        this.lexicon = lexicon;
        this.filters = filters;
        this.phraseTree = new PhraseTree(lexicon.getSubjectiveWords());
    }


    public double calculateSentiment(String tweet) {
        tweet = Filters.chain(tweet, filters);
        List<LexicalToken> lexicalTokens = LexicalParser.lexicallyParseTweet(tweet, phraseTree);
        analyseTokens(lexicalTokens);

        return lexicalTokens.stream().mapToDouble(LexicalToken::getSentimentValue).sum();
    }

    private void analyseTokens(List<LexicalToken> lexicalTokens) {
        for(int i = 0; i < lexicalTokens.size(); i++) {
            LexicalToken token = lexicalTokens.get(i);
            String phrase = token.getPhrase();

            if(token.isInLexicon()) {
                token.setLexicalValue(lexicon.getPolarity(phrase));

            } else if(WordFilters.isEmoteClass(phrase)) {
                token.setLexicalValue(WordFilters.getEmoteClassValue(phrase));

            } else if(WordFilters.isNegation(phrase)) {
                propagateNegation(lexicalTokens, i);

            } else if(WordFilters.isIntensifier(phrase)) {
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
        if (! lexicalTokens.get(index).isAtTheEndOfSentence()) {
            lexicalTokens.get(index + 1).setIntensification(intensification);
        }
    }
}
