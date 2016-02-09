package com.freva.masteroppgave.preprocessing.utils;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGrams {
    private static final Pattern punctuation = Pattern.compile("[!?.]");


    /**
     * Generates all n-grams from a sentence (f.ex. "I like cats" with n=2 returns
     * "I like", "I", "like cats", "like" and "cats")
     * @param text String to generate n-grams for
     * @param n Maximum n-gram length limit
     * @return String array with all n-grams
     */
    public static String[] getNGrams(String text, int n) {
        String[] words = RegexFilters.WHITESPACE.split(text);
        return getNGrams(words, n);
    }


    public static String[] getNGrams(String[] tokens, int n) {
        String[] nGrams = new String[getNumberNGrams(tokens.length, n)];

        for(int offset=0, numNGrams=0; offset<tokens.length; offset++) {
            for(int range=offset+1; range<=offset+n && range<=tokens.length; range++)  {
                nGrams[numNGrams++] = StringUtils.join(Arrays.copyOfRange(tokens, offset, range), " ");
            }
        }
        return nGrams;
    }



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
