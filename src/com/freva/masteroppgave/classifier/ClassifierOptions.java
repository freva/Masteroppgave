package com.freva.masteroppgave.classifier;

import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class ClassifierOptions {
    private static Map<String, Double> options;
    private static Map<String, Double> intensifiers;
    private static Set<String> negators;
    private static Set<String> stopWords;

    static {
        try {
            Words words = JSONUtils.fromJSON(FileUtils.readEntireFileIntoString(new File("res/data/words.json")), new TypeToken<Words>(){});
            options = words.options;
            intensifiers = words.intensifiers;
            negators = words.negators;
            stopWords = words.stopWords;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean containsStopWord(String[] words) {
        for (String word: words) {
            if (stopWords.contains(word)) {
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

        return false;
    }


    public static boolean isIntensifier(String word) {
        return intensifiers.containsKey(word);
    }

    public static double getIntensifierValue(String word) {
        return intensifiers.get(word);
    }

    public static void setIntensifierValue(String word, double value) {
        intensifiers.put(word, value);
    }

    public static boolean isSpecialClassWord(String word) {
        return word.startsWith("||")  && word.endsWith("||");
    }

    public static double getVariable(Variable variable) {
        return options.get(variable.name());
    }

    public static void setVariable(Variable variable, double value) {
        options.put(variable.name(), value);
    }

    public static String getAsJson() {
        return JSONUtils.toJSON(Arrays.asList(options, intensifiers, negators, stopWords), true);
    }

    public enum Variable {
        NEGATION_VALUE, EXCLAMATION_INTENSIFIER, QUESTION_INTENSIFIER, NEGATION_SCOPE_LENGTH
    }

    private class Words {
        private Map<String, Double> options;
        private Map<String, Double> intensifiers;
        private Set<String> negators;
        private Set<String> stopWords;

        private Words(Map<String, Double> options, Map<String, Double> intensifiers, Set<String> negators, Set<String> stopWords) {
            this.options = options;
            this.intensifiers = intensifiers;
            this.negators = negators;
            this.stopWords = stopWords;
        }
    }
}