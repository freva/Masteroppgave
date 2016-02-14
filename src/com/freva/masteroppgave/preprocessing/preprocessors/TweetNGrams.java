package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.utils.NGrams;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class TweetNGrams implements Progressable {
    private int totalLines = 0;
    private int lineCounter = 0;

    /**
     * Writes all frequent n-grams found in a file to another file in JSON format
     * @param input_filename File path to tweets to generate n-grams for
     * @param output_filename File path to write n-grams to
     * @param frequencyCutoff Percentage of total number of tweets that n-gram must have appeared in to be included
     * @throws IOException
     */
    public void createNGrams(String input_filename, String output_filename, double frequencyCutoff) throws IOException {
        this.totalLines = FileUtils.countLines(input_filename);
        Map<String, List<Integer>> nGramsCounter = new HashMap<>();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");

        try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
            for(String line; (line = br.readLine()) != null; lineCounter++) {
                if(lineCounter % 50000 == 0 && lineCounter != 0) {
                    removeEntriesUnderThreshold(nGramsCounter, (int) (frequencyCutoff * lineCounter) / 2);
                }

                line = Filters.chain(line,
                        Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                        Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                        Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText,
                        Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

                for(String nGram: NGrams.getSyntacticalNGrams(line, 6)) {
                    if(! containsAlphabet.matcher(nGram).find()) continue;

                    if(! nGramsCounter.containsKey(nGram)) {
                        nGramsCounter.put(nGram, new ArrayList<>());
                    }

                    nGramsCounter.get(nGram).add(lineCounter);
                }
            }
        }

        removeEntriesUnderThreshold(nGramsCounter, (int) (frequencyCutoff*lineCounter));
        try(Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "UTF-8"))) {
            for(Map.Entry<String, List<Integer>> entry: nGramsCounter.entrySet()) {
                HashSet<Integer> uniqueIDs = new HashSet<>(entry.getValue());
                output.write("{\"" + entry.getKey() + "\": " + uniqueIDs + "}\n");
            }
        }
    }


    private static void removeEntriesUnderThreshold(Map<String, List<Integer>> map, int thresh) {
        Iterator<Map.Entry<String, List<Integer>>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<Integer>> entry = iter.next();
            if (entry.getValue().size() < thresh) {
                iter.remove();
            }
        }
    }

    @Override
    public double getProgress() {
        return (totalLines == 0 ? 0 : 100.0*lineCounter/totalLines);
    }
}