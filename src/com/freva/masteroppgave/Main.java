package com.freva.masteroppgave;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.lexicon.LexiconCreator;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.CanonicalForm;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetNGramsPMI;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.progressbar.ProgressBar;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.reader.LineReader;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Main {
    public static final List<Function<String, String>> N_GRAM_STRING_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername, Filters::removeEmoticons,
            Filters::removeFreeDigits, String::toLowerCase);
    public static final List<Function<String, String>> N_GRAM_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalText, CanonicalForm::correctWordViaCanonical);
    public static final Filters N_GRAM_FILTERS = new Filters(N_GRAM_STRING_FILTERS, N_GRAM_CHARACTER_FILTERS);

    public static final List<Function<String, String>> TWEET_STRING_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, Filters::parseUnicodeEmojisToAlias, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::protectHashtag, Filters::removeEMail, Filters::removeUsername,
            Filters::removeFreeDigits, Filters::parseEmoticons, String::toLowerCase);
    public static final List<Function<String, String>> TWEET_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, CanonicalForm::correctWordViaCanonical);
    public static final Filters TWEET_FILTERS = new Filters(TWEET_STRING_FILTERS, TWEET_CHARACTER_FILTERS);


    public static void main(String[] args) {
        try {
            continuousClassification(new File(args[0]), new File(args[1]), new File(args[2]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void continuousClassification(File lexicon, File options, File dictionary) throws IOException {
        ClassifierOptions.loadOptions(options);
        CanonicalForm.loadDictionary(dictionary);

        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(lexicon);
        Classifier classifier = new Classifier(priorPolarityLexicon, LexicalClassifier.CLASSIFIER_FILTERS);

        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            String tweet = input.nextLine();
            System.out.println(classifier.calculateSentiment(tweet));
        }
    }

    public static void createLexicon(File nGramsFile, File dataSetFile, File lexiconFile, double maxErrorRate, double sentimentValueThreshold) throws IOException {
        Set<String> frequentNGrams = JSONUtils.fromJSONFile(nGramsFile, new TypeToken<Set<String>>(){});
        DataSetReader dataSetReader = new DataSetReader(dataSetFile, 1, 0);

        LexiconCreator lexiconCreator = new LexiconCreator();
        ProgressBar.trackProgress(lexiconCreator, "Creating lexicon...");
        Map<String, Double> lexicon = lexiconCreator.createLexicon(dataSetReader, frequentNGrams, maxErrorRate, sentimentValueThreshold, TWEET_FILTERS);
        JSONUtils.toJSONFile(lexiconFile, MapUtils.sortMapByValue(lexicon), true);
    }

    public static void generateNGrams(File input, File output, int nGramRange, double cutoffFrequency, double PMIValueThreshold) throws IOException {
        TweetNGramsPMI tweetNGrams = new TweetNGramsPMI();
        ProgressBar.trackProgress(tweetNGrams, "Generating tweet n-grams...");
        List<String> ngrams = tweetNGrams.getFrequentNGrams(new LineReader(input), nGramRange, cutoffFrequency, PMIValueThreshold, N_GRAM_FILTERS);

        JSONUtils.toJSONFile(output, ngrams, true);
    }
}
