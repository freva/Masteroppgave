package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PriorPolarityLexicon {
    private final Map<String, Double> polarityLexicon;

    public PriorPolarityLexicon(File file) throws IOException {
        this(readLexicon(file));
    }

    public PriorPolarityLexicon(Map<String, Double> polarityLexicon) {
        this.polarityLexicon = polarityLexicon;
    }

    public double getTokenPolarity(String phrase) {
        return polarityLexicon.get(phrase);
    }

    public boolean hasToken(String word) {
        return polarityLexicon.containsKey(word);
    }

    public Collection<String> getSubjectiveWords() {
        return new ArrayList<>(polarityLexicon.keySet());
    }

    public Map<String, Double> getLexicon() {
        return new HashMap<>(polarityLexicon);
    }


    public static Map<String, Double> readLexicon(File file) throws IOException {
        return JSONUtils.fromJSONFile(file, new TypeToken<Map<String, Double>>() {});
    }
}
