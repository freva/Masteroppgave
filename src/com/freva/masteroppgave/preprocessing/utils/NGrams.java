package com.freva.masteroppgave.preprocessing.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class NGrams {
    public static String[] getNGrams(String text, int nGramLimit) {
        String[] words = text.split(" ");
        String[] nGrams = new String[getNumberNGrams(words.length, nGramLimit)];

        for(int offset=0, numNGrams=0; offset<words.length; offset++) {
            for(int range=offset; range<offset+nGramLimit && range<words.length; range++)  {
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
