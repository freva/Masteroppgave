package com.freva.masteroppgave.preprocessing;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.utils.NGrams;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;


public class GenerateNGrams {
    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        final String input_filename = "res/tweets/200k.txt";
        final String output_filename = "res/tweets/ngrams.txt";
        final HashMap<String, Integer> nGramsCounter = new HashMap<>();
        final Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "utf-8"))) {
            try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
                for(String line; (line = br.readLine()) != null; ) {
                    line = filter(line);

                    for(String nGram: NGrams.getNGrams(line, 6)) {
                        if(! containsAlphabet.matcher(nGram).find()) continue;
                        if(nGramsCounter.containsKey(nGram)) {
                            nGramsCounter.put(nGram, nGramsCounter.get(nGram) + 1);
                        } else {
                            nGramsCounter.put(nGram, 1);
                        }
                    }
                }
            }

            Iterator<Map.Entry<String, Integer>> iter = nGramsCounter.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                if(entry.getValue() < 5) {
                    iter.remove();
                }
            }

            Map.Entry<String, Integer>[] sortedMap = nGramsCounter.entrySet().toArray(new Map.Entry[nGramsCounter.size()]);
            Arrays.sort(sortedMap, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

            for (Map.Entry<String, Integer> e : sortedMap) {
                writer.write(e.getKey() + " : " + e.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("In: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");
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