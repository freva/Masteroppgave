package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class PriorPolarityLexicon implements Iterable<String> {
    private Map<String, Double> polarityLexicon;

    public PriorPolarityLexicon(File file) throws IOException {
        polarityLexicon = readLexicon(file);
    }

    public double getPolarity(String phrase) {
        return polarityLexicon.get(phrase);
    }

    public boolean hasWord(String word) {
        return polarityLexicon.containsKey(word);
    }

    public Set<String> getSubjectiveWords() {
        return new HashSet<>(polarityLexicon.keySet());
    }

    @Override
    public Iterator<String> iterator() {
        return polarityLexicon.keySet().iterator();
    }

    public Map<String, Double> getLexicon() {
        return new HashMap<>(polarityLexicon);
    }


    public static Map<String, Double> readLexicon(File file) throws IOException {
        String json = FileUtils.readEntireFileIntoString(file);
        return JSONUtils.fromJSON(json, new TypeToken<HashMap<String, Double>>(){});
    }
}
