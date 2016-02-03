package com.freva.masteroppgave.preprocessing;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.utils.GatePosTagger;
import java.io.*;

public class TweetsTagger {
    public static void main(String[] args) throws Exception {
        GatePosTagger tagger = new GatePosTagger("res/gate_pos_tagger/models/gate-EN-twitter-fast.model");
        String input_filename = "res/tweets/10k.txt";
        String output_filename = "res/tweets/tagged.txt";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "utf-8"))) {
            try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
                for(String line; (line = br.readLine()) != null; ) {
                    line = filter(line);
                    line = tagger.tagSentence(line);

                    writer.write(line + "\n");
                }
            }
        }
    }

    private static String filter(String text) {
        text = Filters.HTMLUnescape(text);
        text = Filters.removeUnicodeEmoticons(text);
        text = Filters.normalizeForm(text);
        text = Filters.removeURL(text);
        text = Filters.removeRTTag(text);
        text = Filters.removeHashtag(text);
        text = Filters.removeUsername(text);
        text = Filters.removeEmoticons(text);
        text = Filters.removeRepeatedWhitespace(text);
        return text.trim();
    }
}
