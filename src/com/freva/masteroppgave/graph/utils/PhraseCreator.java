package com.freva.masteroppgave.graph.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseCreator {
    private static final HashMap<String, String[][]> descriptivePhrases = new HashMap<>();
    private static final HashMap<String, ArrayList<Integer>> phrasesAndWords = new HashMap<>();
    private static final Pattern startTags = Pattern.compile("\\S+_(JJ|NN|RB)");
    private static final Pattern nonAlphabetic = Pattern.compile("[^a-z ]");
    private static final Pattern posTag = Pattern.compile("_[A-Z]+\\b");

    static {
        descriptivePhrases.put("JJ", new String[][]{new String[]{"_NN"}, new String[]{"_JJ", "_NN"}});
        descriptivePhrases.put("RB", new String[][]{new String[]{"_JJ", "_NN"}, new String[]{"_VB"}});
        descriptivePhrases.put("NN", new String[][]{new String[]{"_JJ", "_NN"}});
    }


    private static void createPhraseAndWordHashMap() throws IOException {
        int lineCounter = 0;
        try(BufferedReader br = new BufferedReader(new FileReader("res/tweets/tagged.txt"))) {
            for(String line; (line = br.readLine()) != null; lineCounter++) {
                String[] words = line.split(" ");
                for (int j = 0; j < words.length - 1; j++) {
                    Matcher tagMatcher = startTags.matcher(words[j]);
                    if (tagMatcher.find()) {
                        String[] phraseCandidate = Arrays.copyOfRange(words, j, Math.min(j + 3, words.length));
                        checkConsecutiveWords(phraseCandidate, tagMatcher.group(1), lineCounter);
                    }
                }
            }
        }
    }

    private static void checkConsecutiveWords(String[] phrase, String tag, int index) {
        String possibleTags[][] = descriptivePhrases.get(tag);
        if(phrase.length == 3) {
            for(String[] possibleTag : possibleTags) {
                if(phrase[1].endsWith(possibleTag[0])) {
                    if(possibleTag.length > 1) {
                        if(phrase[2].matches(possibleTag[1])) {
                            continue;
                        }
                    }
                    createAndAddPhrase(phrase, index);
                }
            }
        }
        else {
            for(String[] possibleTag : possibleTags) {
                if (phrase[1].matches(possibleTag[0])) {
                    createAndAddPhrase(phrase, index);
                    break;
                }
            }
        }
    }

    private static void createAndAddPhrase(String[] phrase, int index) {
        String finalPhrase = posTag.matcher(phrase[0] + " " + phrase[1]).replaceAll("");
        finalPhrase = finalPhrase.toLowerCase();
        finalPhrase = nonAlphabetic.matcher(finalPhrase).replaceAll("");
        if(!phrasesAndWords.containsKey(finalPhrase))
            phrasesAndWords.put(finalPhrase, new ArrayList<>());

        phrasesAndWords.get(finalPhrase).add(index);
    }


    public static void main(String[] args) throws IOException{
        createPhraseAndWordHashMap();
        for(String key : phrasesAndWords.keySet()) {
            System.out.println(key + "\t" + phrasesAndWords.get(key).size());
        }
    }
}
