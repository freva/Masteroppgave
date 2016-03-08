package com.freva.masteroppgave;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.freva.masteroppgave.utils.tools.NGrams;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;;


public class LexicalCreatorPMI implements Progressable{

    private TweetReader tweetReader;

    public static final List<Function<String, String>> filters = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::hashtagToWord, Filters::removeUsername, Filters::replaceEmoticons,
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
            Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

    public LexicalCreatorPMI() throws IOException {
        tweetReader = new TweetReader(new File("res/tweets/classified.txt"));
    }

    public void createLexicon() throws IOException {
        tweetReader = new TweetReader(new File("res/tweets/classified.txt"));
        HashMap<String, Integer> wordsPos = new HashMap<>();
        HashMap<String, Integer> wordsNeg = new HashMap<>();
        long pos = 0;
        long neg = 0;
        while (tweetReader.hasNext()){
            DataSetEntry entry = tweetReader.readAndPreprocessNextDataSetEntry(1, 0);
            String tweet = Filters.chain(entry.getTweet(), filters);
            String[][] nGrams = NGrams.getSyntacticalNGrams(tweet, 3);
            if(entry.getClassification() == DataSetEntry.Class.POSITIVE){
                pos ++;
            }
            else {
                neg++;
            }
            for(String[] ngramWords : nGrams) {
                if(WordFilters.containsIntensifier(ngramWords)) continue;
                if(WordFilters.isStopWord(ngramWords[ngramWords.length - 1])) continue;
                String ngram = String.join(" ", ngramWords);
                if(entry.getClassification() == DataSetEntry.Class.POSITIVE){
                    if(!wordsPos.containsKey(ngram)){
                        wordsPos.put(ngram, 1);
                    }
                    if(!wordsNeg.containsKey(ngram)){
                        wordsNeg.put(ngram, 0);
                    }
                    int newValue = wordsPos.get(ngram) + 1;
                    wordsPos.put(ngram, newValue);
                }
                else{
                    if(!wordsNeg.containsKey(ngram)){
                        wordsNeg.put(ngram, 1);
                    }
                    if(!wordsPos.containsKey(ngram)){
                        wordsPos.put(ngram, 0);
                    }
                    int newValue = wordsNeg.get(ngram) + 1;
                    wordsNeg.put(ngram, newValue);
                }
            }
        }
        HashMap<String, Double> lexicon = new HashMap<>();
        for(String key : wordsPos.keySet()){
            if(wordsNeg.get(key) > 50 || wordsPos.get(key) > 50) {
                long under = wordsNeg.get(key) * pos != 0 ? wordsNeg.get(key) * pos : 1;
                long over = wordsPos.get(key) * neg != 0 ? wordsPos.get(key) * neg : 1;
                double sentimentValue = Math.log((float) over / (float) under);
                lexicon.put(key, sentimentValue);
            }
        }
        String lexiconJson  = JSONUtils.toJSON(MapUtils.sortMapByValue(lexicon), true);
        FileUtils.writeToFile(new File("res/tweets/nrcLexicon.txt"), lexiconJson);
    }

    public static void main(String[] args) throws IOException {
        LexicalCreatorPMI lexicalCreatorPMI = new LexicalCreatorPMI();
        ProgressBar.trackProgress(lexicalCreatorPMI, "Creating lexicon");
        lexicalCreatorPMI.createLexicon();
    }

    @Override
    public double getProgress() {
        return tweetReader.getProgress();
    }
}
