package com.freva.masteroppgave.emoji;


import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class EmojiDict {

    private static final File emojisFile = new File("res/data/emojis.json");
    private static final File afinnFile = new File("res/data/afinn111.json");
    private static final File emojiLexiconFile = new File("res/data/emojiLexicon.json");

    public static void main(String[] args) throws IOException {
        String afinnJson = FileUtils.readEntireFileIntoString(afinnFile);
        HashMap<String, Double> afinnLexicon = JSONUtils.fromJSON(afinnJson, new TypeToken<HashMap<String, Double>>(){});
        String json = FileUtils.readEntireFileIntoString(emojisFile);
        JSONArray jsonArray = new JSONArray(json);
        HashMap<String, Double> emojiLexicon = new HashMap<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONArray aliases = (JSONArray) jsonObject.get("aliases");
            JSONArray jsonTags = (JSONArray) jsonObject.get("tags");
            String[] tags = new String[jsonTags.length()];
            for(int j = 0; j < tags.length; j++) {
                tags[j] = jsonTags.getString(j);
            }
            String alias = aliases.getString(0);
            if(tags.length > 0 && !tags[0].equals("flag")){
                double sentimentScore = 0;
                System.out.println(tags[0] + " " + afinnLexicon.size());
                for(String tag : tags) {
                    if(afinnLexicon.containsKey(tag)) {
                        sentimentScore += afinnLexicon.get(tag);
                    }
                }
                sentimentScore = sentimentScore/tags.length;
                if(sentimentScore != 0) {
                    emojiLexicon.put(alias, sentimentScore/tags.length);
                }
            }
        }
        String JSONNGrams = JSONUtils.toJSON(emojiLexicon, true);
        FileUtils.writeToFile(emojiLexiconFile, JSONNGrams);
    }
}
