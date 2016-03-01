package com.freva.masteroppgave.emoji;


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

public class EmojiDict {
    private static final File emojiLexiconFile = new File("res/data/emojiLexicon.json");

    public static void main(String[] args) throws IOException {
        PriorPolarityLexicon AFINN = new PriorPolarityLexicon(Resources.AFINN_LEXICON);

        Map<String, Double> emojiLexicon = new HashMap<>();
        for(Emoji emoji: EmojiManager.getAll()) {
            if(emoji.getTags().size() > 0 && ! emoji.getTags().contains("flag")){
                double sentimentScore = emoji.getTags().stream()
                        .mapToDouble(i-> AFINN.hasWord(i) ? AFINN.getPolarity(i) : 0)
                        .average().getAsDouble();

                if(sentimentScore != 0) {
                    emojiLexicon.put(emoji.getAliases().get(0), sentimentScore);
                }
            }
        }

        String JSONNGrams = JSONUtils.toJSON(emojiLexicon, true);
        FileUtils.writeToFile(emojiLexiconFile, JSONNGrams);
    }
}
