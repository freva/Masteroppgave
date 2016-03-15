package com.freva.masteroppgave.classifier.sentence;

import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import java.util.*;
import java.util.stream.Collectors;


public class LexicalParser {

    /**
     * Returns list of LexicalTokens found in tweet. The list contains all the words in original tweet, but are
     * optimally grouped up to form largest matching n-grams from lexicon. If no match is found, token is added as
     * singleton with inLexicon value set to false.
     * @param tweet Tweet to lexically parse
     * @param phraseTree Token tree that contains all the lexical n-grams
     * @return List of LexicalTokens
     */
    public static List<LexicalToken> lexicallyParseTweet(String tweet, TokenTrie phraseTree) {
        List<LexicalToken> lexicalTokens = new ArrayList<>();

        for(String sentence: RegexFilters.SENTENCE_END_PUNCTUATION.split(tweet)) {
            String[] sentenceTokens = RegexFilters.WHITESPACE.split(sentence);
            List<String> tokenizedSentence = phraseTree.findOptimalTokenization(sentenceTokens);

            lexicalTokens.addAll(tokenizedSentence.stream().map(LexicalToken::new).collect(Collectors.toList()));
            lexicalTokens.get(lexicalTokens.size()-1).setAtEndOfSentence(true);
        }

        return lexicalTokens;
    }
}
