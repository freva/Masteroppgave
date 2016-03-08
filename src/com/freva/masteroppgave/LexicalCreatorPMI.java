package com.freva.masteroppgave;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.Resources;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.NGrams;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class LexicalCreatorPMI implements Progressable{
    private TweetReader tweetReader;

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
        tweetReader = new TweetReader(new File("res/tweets/classified.txt"));

        int pos = 0, neg = 0;
        Map<String, Integer> wordsPos = new HashMap<>();
        Map<String, Integer> wordsNeg = new HashMap<>();
        while (tweetReader.hasNext()){
            DataSetEntry entry = tweetReader.readAndPreprocessNextDataSetEntry(1, 0);
            String tweet = Filters.chain(entry.getTweet(), filters);
            String[][] nGrams = NGrams.getSyntacticalNGrams(tweet, 3);

            if(entry.getClassification().isPositive()){
                pos++;
            } else {
                neg++;
            }

            for(String[] ngramWords : nGrams) {
                if(WordFilters.containsIntensifier(ngramWords)) continue;
                if(WordFilters.isStopWord(ngramWords[ngramWords.length - 1])) continue;
                String ngram = String.join(" ", ngramWords);
                if(entry.getClassification().isPositive()){
                    MapUtils.incrementMapByValue(wordsPos, ngram, 1);
                } else {
                    MapUtils.incrementMapByValue(wordsNeg, ngram, 1);
                }
            }
        }

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
        String lexiconJson  = JSONUtils.toJSON(MapUtils.sortMapByValue(lexicon), true);
        FileUtils.writeToFile(Resources.PMI_LEXICON, lexiconJson);
    }

    @Override
    public double getProgress() {
        return tweetReader == null ? 0 : tweetReader.getProgress();
    }
}
