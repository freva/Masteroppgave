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

            List<PhraseTree.Phrase> phraseRanges = phraseTree.findTrackedWords(sentenceTokens);
            Collections.sort(phraseRanges);

            phraseRanges = findOptimalAllocation(phraseRanges, 1);
            Collections.sort(phraseRanges, ((o1, o2) -> o1.getStartIndex()-o2.getStartIndex()));

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
        }

        return lexicalTokens;
    }


    private static List<PhraseTree.Phrase> findOptimalAllocation(List<PhraseTree.Phrase> phraseRanges, int offset) {
        if(offset >= phraseRanges.size()) return phraseRanges;
        Iterator<PhraseTree.Phrase> iter = phraseRanges.listIterator(offset);

        while(iter.hasNext()) {
            PhraseTree.Phrase candidate = iter.next();
            for(int i=0; i<offset; i++) {
                if(phraseRanges.get(i).overlapsWith(candidate)) {
                    iter.remove();
                    break;
                }
            }
        }

        return findOptimalAllocation(phraseRanges, offset+1);
    }
}
