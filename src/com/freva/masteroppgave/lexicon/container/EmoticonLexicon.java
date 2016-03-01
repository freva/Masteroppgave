package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.Resources;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Map;

public class EmoticonLexicon {
    private static Map<String, String> lexicon;

    static {
        try {
            String json = FileUtils.readEntireFileIntoString(Resources.EMOTICON_LEXICON);
            lexicon = JSONUtils.fromJSON(json, new TypeToken<Map<String, String>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String replaceEmoticons(String text) {
        StringBuilder replacementText = new StringBuilder();
        for(String token : RegexFilters.WHITESPACE.split(text)) {
            replacementText.append(" ");
            if(lexicon.containsKey(token)) {
                replacementText.append(lexicon.get(token));
            } else {
                replacementText.append(token);
            }
        }
        return replacementText.toString().substring(1);
    }
}
