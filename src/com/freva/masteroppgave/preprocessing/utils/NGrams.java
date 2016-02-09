package com.freva.masteroppgave.preprocessing.utils;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class NGrams {
    /**
     * Generates all n-grams from a sentence (f.ex. "I like cats" with n=2 returns
     * "I like", "I", "like cats", "like" and "cats")
     * @param text String to generate n-grams for
     * @param n Maximum n-gram length limit
     * @return String array with all n-grams
     */
    public static String[] getNGrams(String text, int n) {
        String[] words = RegexFilters.WHITESPACE.split(text);
        String[] nGrams = new String[getNumberNGrams(words.length, n)];

        for(int offset=0, numNGrams=0; offset<words.length; offset++) {
            for(int range=offset; range<offset+n && range<words.length; range++)  {
                nGrams[numNGrams++] = StringUtils.join(Arrays.copyOfRange(words,offset, range), " ");
            }
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
