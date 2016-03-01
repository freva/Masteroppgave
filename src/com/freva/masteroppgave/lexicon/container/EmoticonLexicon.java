package com.freva.masteroppgave.lexicon.container;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.Resources;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

public class EmoticonLexicon {

    private static HashMap<String, String> lexicon;
    private static EmoticonLexicon instance = null;

    private EmoticonLexicon(){
        String JsonLexicon = null;
        try {
            JsonLexicon = FileUtils.readEntireFileIntoString(Resources.EMOTICON_LEXICON);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lexicon = JSONUtils.fromJSON(JsonLexicon, new TypeToken<HashMap<String, String>>(){});
    }

    public static EmoticonLexicon getInstance(){
        if(instance == null) {
            instance = new EmoticonLexicon();
        }
        return instance;
    }

    public String replaceEmoticons(String text) {
        String[] tokens = text.split(" ");
        String replacementText = "";
        for(String token : tokens) {
            if(lexicon.containsKey(token)) {
                replacementText += lexicon.get(token)+ " ";
            }
            else {
                replacementText += token + " ";
            }
        }
        return replacementText.trim();
    }

    private void filters (String text, Function<String, String>... filters) {
        text = Filters.chain(text, filters);
        System.out.println(text);
    }


    public static void main(String[] args) throws IOException {
        String test = "Chicago Bulls.. :-) And yes, Im watching it.. Dreams to come true.. :-) :-) :-) to make it more sweeter, ";
        EmoticonLexicon.getInstance().filters(test, Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
                Filters::removeRTTag, Filters::hashtagToWord, Filters::removeUsername,
                Filters::removeInnerWordCharacters, Filters::removeFreeDigits, Filters::replaceEmoticons,
                Filters::removeNonAlphanumericalText, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

    }
}
