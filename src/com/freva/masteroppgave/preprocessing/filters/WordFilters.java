package com.freva.masteroppgave.preprocessing.filters;

import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class WordFilters {
    private static final Pattern SPECIAL_CLASS_WORD = Pattern.compile("\\|\\|\\S+\\|\\|");
    private static Map<String, Double> intensifiers;
    private static Map<String, Double> emoteClasses;
    private static Set<String> negators;
    private static Set<String> stopWords;

    static {
        try {
            Words words = JSONUtils.fromJSON(FileUtils.readEntireFileIntoString(new File("res/data/words.json")), new TypeToken<Words>(){});
            intensifiers = words.intensifiers;
            emoteClasses = words.emotes;
            negators = words.negators;
            stopWords = words.stopWords;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String stopWordsPattern = "\\b(" + StringUtils.join(stopWords, "|") + ")\\b";
    private static final Pattern stopWordsRegex = Pattern.compile(stopWordsPattern);


    public static String replaceStopWords(String text, String replace) {
        return stopWordsRegex.matcher(text.toLowerCase()).replaceAll(replace);
    }

    public static boolean containsStopWord(String[] words) {
        for(String word: words) {
            if(stopWords.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isStopWord(String word) {
        return stopWords.contains(word);
    }


    public static boolean containsNegation(String[] words) {
        for(String word: words) {
            if(negators.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNegation(String word) {
        return negators.contains(word);
    }


    public static boolean containsIntensifier(String[] words) {
        for(String word: words) {
            if(intensifiers.containsKey(word)) {
                return true;
            }
        }

        return false;    }


    public static boolean isIntensifier(String word) {
        return intensifiers.containsKey(word);
    }

    public static double getIntensifierValue(String word) {
        return intensifiers.get(word);
    }

    public static boolean isEmoteClass(String word) {
        return emoteClasses.containsKey(word);
    }

    public static double getEmoteClassValue(String word) {
        return emoteClasses.get(word);
    }


    public static boolean isSpecialClassWord(String word) {
        return SPECIAL_CLASS_WORD.matcher(word).find();
    }


    private class Words {
        private Map<String, Double> intensifiers;
        private Map<String, Double> emotes;
        private Set<String> negators;
        private Set<String> stopWords;

        private Words(Map<String, Double> intensifiers, Map<String, Double> emotes, Set<String> negators, Set<String> stopWords) {
            this.intensifiers = intensifiers;
            this.emotes = emotes;
            this.negators = negators;
            this.stopWords = stopWords;
        }
    }
}