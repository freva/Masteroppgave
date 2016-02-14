package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.utils.NGrams;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;


public class TweetNGrams implements Progressable {
    private TweetReader tweetReader;
    private String output_filename;

    /**
     *
     * @param input_filename File path to tweets to generate n-grams for
     * @param output_filename File path to write n-grams to
     * @param filters Set of filters to apply before generating n-grams
     * @throws IOException
     */
    @SafeVarargs
    public TweetNGrams(String input_filename, String output_filename, Function<String, String>... filters) throws IOException {
        this.output_filename = output_filename;
        this.tweetReader = new TweetReader(input_filename, filters);
    }


    /**
     * Writes all frequent n-grams found in a file to another file in JSON format
     * @param frequencyCutoff Percentage of total number of tweets that n-gram must have appeared in to be included
     * @throws IOException
     */
    public void createFrequentNGrams(double frequencyCutoff) throws IOException {
        Map<String, List<Integer>> nGramsCounter = new HashMap<>();
        Pattern containsAlphabet = Pattern.compile(".*[a-zA-Z]+.*");
        int lineCounter;

        for(lineCounter=0; tweetReader.hasNext(); lineCounter++) {
            if(lineCounter % 50000 == 0 && lineCounter != 0) {
                removeEntriesUnderThreshold(nGramsCounter, (int) (frequencyCutoff * lineCounter) / 2);
            }

            String line = tweetReader.readAndPreprocessNextTweet();
            for(String nGram: NGrams.getSyntacticalNGrams(line, 6)) {
                if(! containsAlphabet.matcher(nGram).find()) continue;

                if(! nGramsCounter.containsKey(nGram)) {
                    nGramsCounter.put(nGram, new ArrayList<>());
                }

                nGramsCounter.get(nGram).add(lineCounter);
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
        return tweetReader.getProgress();
    }
}