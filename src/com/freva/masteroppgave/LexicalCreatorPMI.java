package com.freva.masteroppgave;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.*;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.NGrams;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class LexicalCreatorPMI implements Progressable{
    private DataSetReader dataSetReader;

    public static final List<Function<String, String>> filters = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::hashtagToWord, Filters::removeUsername, Filters::replaceEmoticons,
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
            Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);


    public static void main(String[] args) throws IOException {
        LexicalCreatorPMI lexicalCreatorPMI = new LexicalCreatorPMI();
        ProgressBar.trackProgress(lexicalCreatorPMI, "Creating lexicon...");
        lexicalCreatorPMI.createLexicon();
    }


    public void createLexicon() throws IOException {
        dataSetReader = new DataSetReader(new File("res/tweets/classified.txt"), 1, 0);

        Map<String, Integer> wordsPos = new HashMap<>();
        Map<String, Integer> wordsNeg = new HashMap<>();
        Parallel.For(dataSetReader, entry -> {
            String tweet = Filters.chain(entry.getTweet(), filters);
            String[][] nGrams = NGrams.getSyntacticalNGrams(tweet, 3);

            for(String[] nGramWords : nGrams) {
                if(WordFilters.containsIntensifier(nGramWords)) continue;
                if(WordFilters.containsNegation(nGramWords)) continue;
                if(WordFilters.isStopWord(nGramWords[nGramWords.length - 1])) continue;

                String nGram = String.join(" ", nGramWords);
                if(entry.getClassification().isPositive()){
                    MapUtils.incrementMapByValue(wordsPos, nGram, 1);
                } else {
                    MapUtils.incrementMapByValue(wordsNeg, nGram, 1);
                }
            }
        });

        int pos = wordsPos.values().stream().mapToInt(Integer::valueOf).sum();
        int neg = wordsNeg.values().stream().mapToInt(Integer::valueOf).sum();
        final double ratio = (double) neg / pos;

        Map<String, Double> lexicon = new HashMap<>();
        for(String key : wordsPos.keySet()){
            if(wordsNeg.getOrDefault(key, 0) > 50 || wordsPos.getOrDefault(key, 0) > 50) {
                int over = wordsPos.getOrDefault(key, 1);
                int under = wordsNeg.getOrDefault(key, 1);

                double sentimentValue = Math.log(ratio * over / under);
                lexicon.put(key, sentimentValue);
            }
        }

        JSONUtils.toJSONFile(Resources.PMI_LEXICON, MapUtils.sortMapByValue(lexicon), true);
    }


    @Override
    public double getProgress() {
        return dataSetReader == null ? 0 : dataSetReader.getProgress();
    }
}
