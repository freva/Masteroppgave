package com.freva.masteroppgave.preprocessing.preprocessors;


import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.Resources;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmoteDictionary {
    private static final File emoticonLexiconFile = new File("res/data/EmoticonSentimentLexicon.json");
    private static final File emoticonDictionaryFile = new File("res/data/emoticonDictionary.json");
    private static final String[] emoteClasses = {"emoteNEG", "emoteNEU", "emotePOS"};

    public static void updateLexicon() throws IOException {
        Map<String, Double> emoteLexicon = PriorPolarityLexicon.readLexicon(emoticonLexiconFile);
        emoteLexicon.putAll(getEmojiLexicon());

        Map<String, String> emoteDictionary = new HashMap<>();
        for(String emote: emoteLexicon.keySet()) {
            emoteDictionary.put(emote, emoteClasses[1 + (int) Math.signum(emoteLexicon.get(emote))]);
        }

        String JSONNDictionary = JSONUtils.toJSON(emoteDictionary, true);
        FileUtils.writeToFile(emoticonDictionaryFile, JSONNDictionary);
    }



    /**
     * Extracts all Emojis from vdurmont EmojiManager and assigns them a sentiment value based of average sentiment
     * of tags found in AFINN lexicon
     * @return Map of Unicode Emoji and their sentiment value as Double
     * @throws IOException
     */
    public static Map<String, Double> getEmojiLexicon() throws IOException {
        PriorPolarityLexicon AFINN = new PriorPolarityLexicon(Resources.AFINN_LEXICON);

        Map<String, Double> emojiLexicon = new HashMap<>();
        for(Emoji emoji: EmojiManager.getAll()) {
            if(emoji.getTags().size() > 0 && ! emoji.getTags().contains("flag")){
                double sentimentScore = emoji.getTags().stream().filter(AFINN::hasWord)
                        .mapToDouble(AFINN::getPolarity).average().getAsDouble();

                if(sentimentScore != 0) {
                    emojiLexicon.put(emoji.getUnicode(), sentimentScore);
                }
            }
        }

        return emojiLexicon;
    }
}
