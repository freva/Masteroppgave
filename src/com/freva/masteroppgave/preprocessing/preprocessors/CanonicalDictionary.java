package com.freva.masteroppgave.preprocessing.preprocessors;


import com.freva.masteroppgave.preprocessing.filters.CanonicalForm;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.reader.LineReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CanonicalDictionary implements Progressable {
    public static final List<Function<String, String>> filters = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername, Filters::removeEmoticons,
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
            Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

    private LineReader tweetReader;


    /**
     * Creates canonical dictionary:
     * Words that are reduced to the same canonical form are grouped together, the frequent words are kept in the final
     * dictionary. F.ex. "god" => ["good", "god"].
     * @param input File with words to base dictionary off
     * @param output File to write dictionary to
     * @throws IOException
     */
    public void createCanonicalDictionary(File input, File output, double correctFrequency, double termFrequency) throws IOException {
        tweetReader = new LineReader(input);

        int iteration = 0;
        Map<String, Map<String, Integer>> counter = new HashMap<>();
        for(String tweet: tweetReader) {
            if(iteration++ % 100000 == 0) removeInfrequent(counter, (int) (iteration*termFrequency/2), correctFrequency/2);

            tweet = Filters.stringChain(tweet, filters);
            for(String word: RegexFilters.WHITESPACE.split(tweet)) {
                String reduced = CanonicalForm.reduceToCanonicalForm(word);
                if(! counter.containsKey(reduced)) {
                    counter.put(reduced, new HashMap<>());
                }

                MapUtils.incrementMapByValue(counter.get(reduced), word, 1);
            }
        }


        removeInfrequent(counter, (int) (iteration*termFrequency), correctFrequency);
        Map<String, Set<String>> options = counter.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().keySet()));

        JSONUtils.toJSONFile(output, options, true);
    }


    private static void removeInfrequent(Map<String, Map<String, Integer>> counter, int termLimit, double cutoff) {
        Iterator<Map.Entry<String, Map<String, Integer>>> canonicals = counter.entrySet().iterator();

        while(canonicals.hasNext()) {
            Map.Entry<String, Map<String, Integer>> canonical = canonicals.next();
            int termCounter = canonical.getValue().values().stream().mapToInt(Integer::intValue).sum();

            if (canonical.getValue().size() < 5 || termCounter <= termLimit) {
                canonicals.remove();
                continue;
            }

            Iterator<Map.Entry<String, Integer>> originals = canonical.getValue().entrySet().iterator();
            while(originals.hasNext()) {
                if(originals.next().getValue() < termCounter*cutoff) {
                    originals.remove();
                }
            }
        }
    }

    @Override
    public double getProgress() {
        return tweetReader != null ? tweetReader.getProgress() : 0;
    }
}
