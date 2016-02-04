package com.freva.masteroppgave.lexicon.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PolarityWordsDetector {

    private HashMap<String, Integer> polarityLexicon = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> wordOccurences = new HashMap<>();

    private int lineCounter = 0;

    public PolarityWordsDetector(HashMap<String, Integer> polarityLexicon) {
        this.polarityLexicon = polarityLexicon;
    }

    public void detectPolarityWords(String sentence) {
        for(String word : sentence.split(" ")) {
            if(polarityLexicon.containsKey(word)) {
                if(!wordOccurences.containsKey(word)) {
                    wordOccurences.put(word, new ArrayList<>());
                }
                wordOccurences.get(word).add(lineCounter);
            }
        }
        lineCounter++;
    }

    public HashMap<String, ArrayList<Integer>> getWordOccurences() {
        HashMap<String, ArrayList<Integer>> counter = new HashMap<>();
        for(Map.Entry<String, ArrayList<Integer>> entry : wordOccurences.entrySet()) {
            ArrayList<Integer> clonedList = new ArrayList<>();
            clonedList.addAll(entry.getValue());
            counter.put(entry.getKey(), clonedList);
        }
        return counter;
    }
}
