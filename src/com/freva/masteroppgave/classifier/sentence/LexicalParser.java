package com.freva.masteroppgave.classifier.sentence;

import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import java.util.*;


public class LexicalParser {

    /**
     * Returns list of LexicalTokens found in tweet. The list contains all the words in original tweet, but are
     * optimally grouped up to form largest matching n-grams from lexicon. If no match is found, token is added as
     * singleton with inLexicon value set to false.
     * @param tweet Tweet to lexically parse
     * @param phraseTree Token tree that contains all the lexical n-grams
     * @return List of LexicalTokens
     */
    public static List<LexicalToken> lexicallyParseTweet(String tweet, TokenTrie<String> phraseTree) {
        List<LexicalToken> lexicalTokens = new ArrayList<>();

        for(String sentence: RegexFilters.SENTENCE_END_PUNCTUATION.split(tweet)) {
            String[] sentenceTokens = RegexFilters.WHITESPACE.split(sentence);
            List<TokenTrie<String>.Token> tokenRanges = phraseTree.findOptimalAllocation(sentenceTokens);

            int setIndex = 0;
            for(TokenTrie<String>.Token token : tokenRanges) {
                while(setIndex < token.getStartIndex()) {
                    lexicalTokens.add(new LexicalToken(sentenceTokens[setIndex++], false));
                }
                lexicalTokens.add(new LexicalToken(String.join(" ", token.getTokenSequence()), true));
                setIndex = token.getEndIndex()+1;
            }

            while(setIndex < sentenceTokens.length) {
                lexicalTokens.add(new LexicalToken(sentenceTokens[setIndex++], false));
            }

            lexicalTokens.get(lexicalTokens.size()-1).setAtEndOfSentence(true);
        }

        return lexicalTokens;
    }
}
