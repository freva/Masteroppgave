package com.freva.masteroppgave.preprocessing;


import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.utils.NGrams;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class GenerateNGrams {
    public static HashMap<String, ArrayList<Integer>> createNGrams() {
        long startTime = System.currentTimeMillis();
        String input_filename = "res/tweets/200k.txt";
        String output_filename = "res/tweets/ngrams.txt";
        HashMap<String, ArrayList<Integer>> nGramsCounter = new HashMap<>();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");
        int lineCounter = 0;

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "utf-8"))) {
            try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
                for(String line; (line = br.readLine()) != null; ) {
                    line = filter(line);
                    line = WordFilters.replaceStopWords(line, "_");

                    for(String nGram: NGrams.getNGrams(line, 6)) {
                        if(! containsAlphabet.matcher(nGram).find()) continue;
                        ArrayList<Integer> indexes;
                        if(nGramsCounter.containsKey(nGram)) {
                             indexes = nGramsCounter.get(nGram);
                            indexes.add(lineCounter);
                            nGramsCounter.put(nGram, indexes);
                        } else {
                            indexes = new ArrayList<>();
                            indexes.add(lineCounter);
                            nGramsCounter.put(nGram, indexes);
                        }
                    }
                    lineCounter++;
                }
            }

            Iterator<Map.Entry<String, ArrayList<Integer>>> iter = nGramsCounter.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ArrayList<Integer>> entry = iter.next();
                if(entry.getValue().size() < 20) {
                    iter.remove();
                }
            }

//            Map.Entry<String, Integer>[] sortedMap = nGramsCounter.entrySet().toArray(new Map.Entry[nGramsCounter.size()]);
//            Arrays.sort(sortedMap, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

//            for (Map.Entry<String, ArrayList<Integer>> e : nGramsCounter.entrySet()) {
//                writer.write(e.getKey() + " : " + e.getValue() + "\n");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("In: " + ((System.currentTimeMillis()-startTime)/1000) + "sec");
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
        text = Filters.removeRepeatedWhitespace(text);
        return text.trim();
    }
}