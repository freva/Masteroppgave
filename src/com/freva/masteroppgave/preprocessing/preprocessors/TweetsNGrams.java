package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.utils.NGrams;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.ProgressBar;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class TweetsNGrams {
    /**
     * Writes all frequent n-grams found in a file to another file in JSON format
     * @param input_filename File path to tweets to generate n-grams for
     * @param output_filename File path to write n-grams to
     * @param frequencyCutoff Percentage of total number of tweets that n-gram must have appeared in to be included
     * @throws IOException
     */
    public static void createNGrams(String input_filename, String output_filename, double frequencyCutoff) throws IOException {
        HashMap<String, ArrayList<Integer>> nGramsCounter = new HashMap<>();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");
        int lineCounter = 0;
        ProgressBar progress = new ProgressBar(FileUtils.countLines(input_filename));

        try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
            for(String line; (line = br.readLine()) != null; lineCounter++) {
                line = Filters.chain(line,
                        Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm,
                        Filters::removeURL, Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername,
                        Filters::removeEmoticons, Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText,
                        Filters::removeFreeDigits, Filters::removeRepeatedWhitespace, String::trim);
                progress.printProgress(lineCounter);

                for(String nGram: NGrams.getSyntacticalNGrams(line, 6)) {
                    if(! containsAlphabet.matcher(nGram).find()) continue;

                    if(! nGramsCounter.containsKey(nGram)) {
                        nGramsCounter.put(nGram, new ArrayList<>());
                    }

                    nGramsCounter.get(nGram).add(lineCounter);
                }
            }
        }

        Iterator<Map.Entry<String, ArrayList<Integer>>> iter = nGramsCounter.entrySet().iterator();
        int limit = (int) (frequencyCutoff*lineCounter);
        try(Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "UTF-8"))) {
            while (iter.hasNext()) {
                Map.Entry<String, ArrayList<Integer>> entry = iter.next();
                if (entry.getValue().size() > limit) {
                    HashSet<Integer> uniqueIDs = new HashSet<>(entry.getValue());
                    output.write("{\"" + entry.getKey() + "\": " + uniqueIDs + "}\n");
                }

                iter.remove();
            }
        }
    }
}