package com.freva.masteroppgave.preprocessing.filters;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class WordFilters {
    public static final String[] stopWords = {"a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
            "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
            "these", "they", "this", "to", "was", "will", "with"};
    private static final String pattern = "\\b(" + StringUtils.join(stopWords, "|") + ")\\b";
    private static final Pattern stopWordsRegex = Pattern.compile(pattern);

    public static String replaceStopWords(String text, String replace) {
        return stopWordsRegex.matcher(text.toLowerCase()).replaceAll(replace);
    }

    public static boolean containsStopWord(String text) {
        return stopWordsRegex.matcher(text).find();
    }
}