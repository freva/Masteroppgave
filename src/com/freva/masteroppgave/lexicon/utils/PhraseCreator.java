package com.freva.masteroppgave.lexicon.utils;

import com.freva.masteroppgave.preprocessing.filters.Filters;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseCreator {
    private static final HashMap<String, String[][]> descriptivePhrases = new HashMap<>();
    private static final Pattern startTags = Pattern.compile("\\S+_(JJ|NN|RB)");
    private static final Pattern nonAlphabetic = Pattern.compile("[^a-z ]");

    private HashMap<String, ArrayList<Integer>> phrasesAndWords = new HashMap<>();
    private int sentenceCounter = 0;

    static {
        descriptivePhrases.put("JJ", new String[][]{new String[]{"_NN"}, new String[]{"_JJ", "_NN"}});
        descriptivePhrases.put("RB", new String[][]{new String[]{"_JJ", "_NN"}, new String[]{"_VB"}});
        descriptivePhrases.put("NN", new String[][]{new String[]{"_JJ", "_NN"}});
    }


    public void detectPhrases(String sentence) {
        String[] words = sentence.split(" ");
        for (int j = 0; j < words.length - 1; j++) {
            Matcher tagMatcher = startTags.matcher(words[j]);
            if (tagMatcher.find()) {
                String[] phraseCandidate = Arrays.copyOfRange(words, j, Math.min(j + 3, words.length));
                checkConsecutiveWords(phraseCandidate, tagMatcher.group(1));
            }
        }
        sentenceCounter++;
    }

    private void checkConsecutiveWords(String[] phrase, String tag) {
        String possibleTags[][] = descriptivePhrases.get(tag);
        if(phrase.length == 3) {
            for(String[] possibleTag : possibleTags) {
                if(phrase[1].contains(possibleTag[0])) {
                    if(possibleTag.length > 1) {
                        if(phrase[2].contains(possibleTag[1])) {
                            continue;
                        }
                    }
                    createAndAddPhrase(phrase);
                }
            }
        } else {
            for(String[] possibleTag : possibleTags) {
                if (phrase[1].contains(possibleTag[0])) {
                    createAndAddPhrase(phrase);
                    break;
                }
            }
        }
    }

    private void createAndAddPhrase(String[] phrase) {
        String finalPhrase = Filters.removePosTags(phrase[0] + " " + phrase[1]);
        finalPhrase = finalPhrase.toLowerCase();
        finalPhrase = nonAlphabetic.matcher(finalPhrase).replaceAll("");
        if(!phrasesAndWords.containsKey(finalPhrase))
            phrasesAndWords.put(finalPhrase, new ArrayList<>());

        phrasesAndWords.get(finalPhrase).add(sentenceCounter);
    }

    public HashMap<String, ArrayList<Integer>> getPhrases() {
        HashMap<String, ArrayList<Integer>> counter = new HashMap<>();
        for(Map.Entry<String, ArrayList<Integer>> entry : phrasesAndWords.entrySet()) {
            ArrayList<Integer> clonedList = new ArrayList<>();
            clonedList.addAll(entry.getValue());
            counter.put(entry.getKey(), clonedList);
        }
        return counter;
    }
}
