package com.freva.masteroppgave.classifier.sentence;

import com.freva.masteroppgave.lexicon.container.PhraseTree;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import java.util.*;


public class LexicalParser {

    /**
     * Returns list of LexicalTokens found in tweet. The list contains all the words in original tweet, but are
     * optimally grouped up to form largest matching n-grams from lexicon. If no match is found, token is added as
     * singleton with inLexicon value set to false.
     * @param tweet Tweet to lexically parse
     * @param phraseTree Phrase tree that contains all the lexical n-grams
     * @return List of LexicalTokens
     */
    public static List<LexicalToken> lexicallyParseTweet(String tweet, PhraseTree phraseTree) {
        List<LexicalToken> lexicalTokens = new ArrayList<>();

        for(String sentence: RegexFilters.SENTENCE_END_PUNCTUATION.split(tweet)) {
            String[] sentenceTokens = RegexFilters.WHITESPACE.split(sentence);
            List<PhraseTree.Phrase> phraseRanges = phraseTree.findOptimalAllocation(sentenceTokens);

            int setIndex = 0;
            for(PhraseTree.Phrase phrase: phraseRanges) {
                while(setIndex < phrase.getStartIndex()) {
                    lexicalTokens.add(new LexicalToken(sentenceTokens[setIndex++], false));
                }
                lexicalTokens.add(new LexicalToken(phrase.getPhrase(), true));
                setIndex = phrase.getEndIndex()+1;
            }

            while(setIndex < sentenceTokens.length) {
                lexicalTokens.add(new LexicalToken(sentenceTokens[setIndex++], false));
            }

            lexicalTokens.get(lexicalTokens.size()-1).setAtEndOfSentence(true);
        }

        return lexicalTokens;
    }
}
