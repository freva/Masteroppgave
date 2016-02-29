package com.freva.masteroppgave.emoji;


import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class emojiDict {

    private static final File emojis = new File("res/data/emojis.json");
    private static final File emojiDictionary2 = new File("res/data/emojiDictionary.json");

    public static void main(String[] args) throws IOException {
        String json = FileUtils.readEntireFileIntoString(emojis);
        JSONArray jsonArray = new JSONArray(json);
        HashMap<String, String[]> emojiDictionary = new HashMap<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONArray aliases = (JSONArray) jsonObject.get("aliases");
            JSONArray jsonTags = (JSONArray) jsonObject.get("tags");
            String[] tags = new String[jsonTags.length()];
            for(int j = 0; j < tags.length; j++) {
                tags[j] = jsonTags.getString(j);
            }
            String alias = aliases.getString(0);
            if(tags.length > 0 && !tags[0].equals("flag")) emojiDictionary.put(alias, tags);
        }
        String JSONNGrams = JSONUtils.toJSON(emojiDictionary, true);
        FileUtils.writeToFile(emojiDictionary2, JSONNGrams);
        System.out.println(Filters.replaceUnicodeEmoticons("An 😀awesome 😃string with a few 😉emojis!"));

    }
}
