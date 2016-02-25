package com.freva.masteroppgave.preprocessing.filters;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class WordFilters {
    public static final String[] stopWords = {"a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
            "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
            "these", "they", "this", "to", "was", "will", "with"};

    public static final String[] negators = {"aint", "aint", "anit", "cant", "cannot", "cant", "couldnt",
            "couldnt", "didnt", "didnt", "dnt", "doesnt", "doesnt", "doesnt", "dont", "dont",
            "hadnt", "hasnt", "hasnt", "havent", "havent havnt", "havnt", "isnt", "isnt", "lack",
            "lacking", "lacks", "no", "nor", "not", "shouldnt", "shouldnt", "wasnt", "wasnt",
            "wont", "wont", "wouldnt", "wouldnt"};


    private static final String negatorPattern = "\\b(" + StringUtils.join(negators, "|") + ")\\b";
    private static final Pattern negatorRegex = Pattern.compile(negatorPattern);

    private static final String stopWordsPattern = "\\b(" + StringUtils.join(stopWords, "|") + ")\\b";
    private static final Pattern stopWordsRegex = Pattern.compile(stopWordsPattern);

    public static String replaceStopWords(String text, String replace) {
        return stopWordsRegex.matcher(text.toLowerCase()).replaceAll(replace);
    }

    public static boolean containsStopWord(String text) {
        return stopWordsRegex.matcher(text).find();
    }

    public static boolean containsNegation(String word) {
        return negatorRegex.matcher(word).find();
    }

}