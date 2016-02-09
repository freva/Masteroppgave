package com.freva.masteroppgave.preprocessing.preprocessors;


import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.utils.NGrams;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class TweetsNGrams {
    public static HashMap<String, ArrayList<Integer>> createNGrams(String input_filename, int frequencyCutoff) throws IOException {
        HashMap<String, ArrayList<Integer>> nGramsCounter = new HashMap<>();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");
        int lineCounter = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
            for(String line; (line = br.readLine()) != null; lineCounter++) {
                line = filter(line);

                for(String nGram: NGrams.getNGrams(line, 6)) {
                    if(! containsAlphabet.matcher(nGram).find()) continue;

                    if(! nGramsCounter.containsKey(nGram)) {
                        nGramsCounter.put(nGram, new ArrayList<>());
                    }

                    nGramsCounter.get(nGram).add(lineCounter);
                }
            }
        }

        Iterator<Map.Entry<String, ArrayList<Integer>>> iter = nGramsCounter.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = iter.next();
            if(entry.getValue().size() < frequencyCutoff) {
                iter.remove();
            }
        }

        return nGramsCounter;
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
        text = Filters.removeInnerWordCharacters(text);
        text = Filters.removeNonAlphanumericalText(text);
        text = Filters.removeFreeDigits(text);
        text = Filters.removeStopWords(text);
        text = Filters.removeRepeatedWhitespace(text);
        return text.trim();
    }
}