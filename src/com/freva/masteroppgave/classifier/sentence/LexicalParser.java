package com.freva.masteroppgave.classifier.sentence;

import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import java.util.*;
import java.util.regex.Matcher;
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

        int prev = 0;
        Matcher matcher = RegexFilters.SENTENCE_END_PUNCTUATION.matcher(tweet);
        while (matcher.find()) {
            String sentence = tweet.substring(prev, matcher.start());
            String punctuation = matcher.group();
            prev = matcher.end();

            lexicalTokens.addAll(addSentence(sentence, punctuation, phraseTree));
        }

        lexicalTokens.addAll(addSentence(tweet.substring(prev), null, phraseTree));

        return lexicalTokens;
    }

    private static List<LexicalToken> addSentence(String sentence, String punctuation, TokenTrie phraseTree) {
        String[] sentenceTokens = RegexFilters.WHITESPACE.split(sentence);

        List<String> tokenizedSentence = phraseTree.findOptimalTokenization(sentenceTokens);
        List<LexicalToken> tokens = tokenizedSentence.stream().map(LexicalToken::new).collect(Collectors.toList());

        if(tokens.size() > 0) {
            tokens.get(tokens.size() - 1).setAtEndOfSentence(true);

            if (punctuation != null && punctuation.contains("!")) {
                final double exclamationIntensifier = ClassifierOptions.getVariable(ClassifierOptions.Variable.EXCLAMATION_INTENSIFIER);
                for (LexicalToken token : tokens) {
                    token.intensifyToken(exclamationIntensifier);
                }
            } else if (punctuation != null && punctuation.contains("?")) {
                final double questionIntensifier = ClassifierOptions.getVariable(ClassifierOptions.Variable.QUESTION_INTENSIFIER);
                for (LexicalToken token : tokens) {
                    token.intensifyToken(questionIntensifier);
                }
            }
        }

        return tokens;
    }
}
