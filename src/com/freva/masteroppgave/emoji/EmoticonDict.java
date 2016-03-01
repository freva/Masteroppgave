package com.freva.masteroppgave.emoji;


import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class EmoticonDict {

    private static final File emoticonLexiconFile = new File("res/data/EmoticonSentimentLexicon.json");
    private static final File emoticonDictionaryFile = new File("res/data/emoticonDictionary.json");

    public EmoticonDict() throws IOException {
        String emoticonLexiconJson = FileUtils.readEntireFileIntoString(emoticonLexiconFile);
        HashMap<String, Integer> lexicon = JSONUtils.fromJSON(emoticonLexiconJson, new TypeToken<HashMap<String, Integer>>(){});
        HashMap<String, String> dictionary = integerToStringValues(lexicon);
        String JSONNDictionary = JSONUtils.toJSON(dictionary, true);
        FileUtils.writeToFile(emoticonDictionaryFile, JSONNDictionary);
    }

    private HashMap<String, String> integerToStringValues(HashMap<String, Integer> lexicon) {
        HashMap<String, String> dictionary = new HashMap<>();
        for(String key: lexicon.keySet()) {
            String value = "";
            if(lexicon.get(key) != 0) {
                value = lexicon.get(key) > 0 ? "emotePos" : "emoteNeg";
            }
            else{
                value = "emoteNeutral";
            }
            dictionary.put(key, value);
        }
        return dictionary;
    }

    public static void main(String[] args) throws IOException {
        new EmoticonDict();
    }


}
