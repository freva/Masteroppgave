package com.freva.masteroppgave.graph.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

public class PhraseCreator {

    private HashMap<String, String[][]> descriptivePhrases;
    private HashMap<String, ArrayList<Integer>> phrasesAndWords;



    public PhraseCreator() throws IOException {
        createHashMap();
        createPhraseAndWordHashMap();
    }


    private void createHashMap() {
        descriptivePhrases = new HashMap<>();
        String tags[][][] = {{{"JJ"} , {"_NN"}, {"_JJ", "_NN"}}, {{"RB"} , {"_JJ", "_NN"}, {"_VB"}}, {{"NN"} , {"_JJ"}, {"_NN"}} };
        for(int i = 0; i < tags.length; i++) {
            String followingTags[][] = {tags[i][1],tags[i][2]};
            descriptivePhrases.put(tags[i][0][0], followingTags);
        }
    }

    private void createPhraseAndWordHashMap() throws IOException {
        phrasesAndWords = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(new File("res/tweets/tagged.txt")));
        Stream<String> lines = reader.lines();
        Object[] tweets = lines.toArray();
        for(int i = 0; i < tweets.length; i++) {
            String[] words = ((String)tweets[i]).split(" ");
            for(int j = 0; j < words.length-1; j++) {
                if(words[j].matches("\\S+_(JJ|NN|RB)")) {
                    String[] wordAndTag = words[j].split("_");
                    String[] phrase = {words[j], words[j+1]};
                    String[] test = Arrays.copyOfRange(words, j , j+3);
                    checkConsecutiveWords(test, wordAndTag[wordAndTag.length-1], i);
                }
            }
        }
    }

    private void checkConsecutiveWords(String[] phrase, String tag, int index) {
        String possibleTags[][] = descriptivePhrases.get(tag);
        if(phrase.length == 3) {
            for(String[] possibleTag : possibleTags) {
                if(phrase[1].matches(possibleTag[0])) {
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

    private void createAndAddPhrase(String[] phrase, int index) {
        String finalPhrase = (phrase[0] + " " + phrase[1]).replaceAll("_([A-Z$]*)\\s", "");
        finalPhrase = finalPhrase.toLowerCase();
        finalPhrase = finalPhrase.replaceAll("[^a-z]","");
        ArrayList<Integer> indexes = phrasesAndWords.containsKey(finalPhrase) ? phrasesAndWords.get(finalPhrase) : new ArrayList<>();
        indexes.add(index);
        phrasesAndWords.put(finalPhrase, indexes);
    }


    public static void main(String[] args) throws IOException{
        PhraseCreator phraseCreator = new PhraseCreator();
        for(String key : phraseCreator.phrasesAndWords.keySet()) {
            System.out.println(key + "\t" + phraseCreator.phrasesAndWords.get(key).size());
        }
    }
}
