package com.freva.masteroppgave.preprocessing.utils;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;


public class NGrams {
    private static final Pattern punctuation = Pattern.compile("[!?.]");


    /**
     * Generates all n-grams from a sentence (f.ex. "I like cats" with n=2 returns
     * "I like", "I", "like cats", "like" and "cats"), splits on \\s+ regex.
     * @param text String to generate n-grams for
     * @param n Maximum n-gram length
     * @return String array with all n-grams
     */
    public static String[] getNGrams(String text, int n) {
        String[] words = RegexFilters.WHITESPACE.split(text);
        return getNGrams(words, n);
    }


    /**
     * Generates all the n-grams on already split up tokens.
     * @param tokens Tokens to generate n-gram for
     * @param n Maximum n-gram length
     * @return String with all n-grams
     */
    public static String[] getNGrams(String[] tokens, int n) {
        String[] nGrams = new String[getNumberNGrams(tokens.length, n)];

        for(int offset=0, numNGrams=0; offset<tokens.length; offset++) {
            for(int range=offset+1; range<=offset+n && range<=tokens.length; range++)  {
                nGrams[numNGrams++] = StringUtils.join(Arrays.copyOfRange(tokens, offset, range), " ");
            }
        }
        return nGrams;
    }


    /**
     * Generates all n-grams while also respecting basic punctuation (!?.)
     * F.ex. for sentence "Today is a nice day. I like the sun", the n-grams with n=3 are:
     * "Today", "Today is", "Today is a", "is", "is a", "is a nice", "a", "a nice", "a nice day",
     * "nice", "nice day", "day", "I" "I like", "I like the", "like" "like the", "like the sun",
     * "the", "the sun", "sun". Note that "nice day I", "day I", "day I like" are not included
     * because they are separated by a punctuation.
     * @param text String to generate n-Grams for
     * @param n Maximum n-gram length
     * @return String array with all n-grams
     */
    public static String[] getSyntacticalNGrams(String text, int n) {
        String[] subParts = punctuation.split(text);
        String[][] nGramParts = new String[subParts.length][];
        int length = 0;
        for(int i=0; i<subParts.length; i++) {
            nGramParts[i] = getNGrams(subParts[i].trim(), n);
            length += nGramParts[i].length;
        }

        String[] nGrams = new String[length];
        for(int i=0, offset=0; i<subParts.length; i++) {
            System.arraycopy(nGramParts[i], 0, nGrams, offset, nGramParts[i].length);
            offset += nGramParts[i].length;
        }
        return nGrams;
    }

    private static int getNumberNGrams(int length, int scope) {
        if (length <= scope) {
            return length*(length+1)/2;
        } else {
            return (length-scope+1)*scope + (scope-1)*scope/2;
        }
    }
}
