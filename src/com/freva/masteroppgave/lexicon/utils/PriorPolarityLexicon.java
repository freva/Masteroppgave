package com.freva.masteroppgave.lexicon.utils;

import com.freva.masteroppgave.utils.FileUtils;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class PriorPolarityLexicon {
    private HashMap<String, Integer> polarityLexicon;

    public PriorPolarityLexicon(File file) throws IOException {
        polarityLexicon =FileUtils.readObjectFromJSONFile(file, new TypeToken<HashMap<String, Integer>>(){});
    }

    public int getPolarity(String phrase) {
        if(polarityLexicon.containsKey(phrase)) {
            return polarityLexicon.get(phrase);
        }
        return 0;
    }

    public boolean hasWord(String word) {
        return polarityLexicon.containsKey(word);
    }

    public Set<String> getSubjectiveWords() {
        return new HashSet<>(polarityLexicon.keySet());
    }
}
