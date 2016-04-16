package com.freva.masteroppgave;

import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.lexicon.LexiconCreator;
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
            Filters::removeFreeDigits, Filters::replaceEmoticons, String::toLowerCase);
    public static final List<Function<String, String>> TWEET_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, CanonicalForm::correctWordViaCanonical);
    public static final Filters TWEET_FILTERS = new Filters(TWEET_STRING_FILTERS, TWEET_CHARACTER_FILTERS);


    public static void main(String[] args) {
//        args = new String[]{"ngrams", "tweets=res/tweets/filtered.txt", "output=res/tweets/ngrams.txt", "options=res/data/options.pmi.json", "n=6", "minRatio=0.000005", "minPMI=0"};
//        args = new String[]{"ngrams", "tweets=res/tweets/filtered.txt", "output=res/tweets/ngrams.txt", "options=res/data/options.pmi.json", "n=5", "minRatio=0.000005", "minPMI=4"};
        args = new String[]{"create", "ngrams=res/tweets/ngrams.txt", "dataset=res/tweets/classified.txt", "output=res/data/lexicon.pmi.json", "options=res/data/options.pmi.json", "maxError=300", "minSentiment=2.0"};
        if (args.length == 0) {
            printHelp();
            return;
        }

        Hashtable<String, String> arguments = new Hashtable<>();
        for(int i = 1; i < args.length; i++) {
            String[] option = args[i].split("=");
            arguments.put(option[0], (option.length == 2 ? option[1] : ""));
        }

        try {
            File options = new File(arguments.get("options"));
            ClassifierOptions.loadOptions(options);

            switch (args[0]) {
                case "ngrams":
                    final File tweets = new File(arguments.get("tweets"));
                    final File output = new File(arguments.get("output"));
                    final int nGramRange = Integer.parseInt(arguments.get("n"));
                    final double minRatio = Double.parseDouble(arguments.get("minRatio"));
                    final double minPMI = Double.parseDouble(arguments.get("minPMI"));
                    generateNGrams(tweets, output, nGramRange, minRatio, minPMI);
                    break;

                case "create":
                    final File ngrams = new File(arguments.get("ngrams"));
                    final File dataset = new File(arguments.get("dataset"));
                    final File lexicon = new File(arguments.get("output"));
                    final double maxErrorRate = Double.parseDouble(arguments.get("maxError"));
                    final double minSentiment = Double.parseDouble(arguments.get("minSentiment"));
                    createLexicon(ngrams, dataset, lexicon, maxErrorRate, minSentiment);
                    break;

                default:
                    printHelp();
            }
        } catch (NullPointerException e) {
            System.err.println("Missing parameter!");
            printHelp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void printHelp() {
        System.out.println("Description of modes and their parameters. To run, use [mode] [param1]=[value1] [param2]=[value2] etc");
        System.out.println("ngrams - to create n-grams set");
        System.out.println("    tweets - path to file containing text for which n-grams will be generated");
        System.out.println("    output - path to file where n-grams will be written");
        System.out.println("    options - path to file with classifier options containing stop words and intensifiers");
        System.out.println("    n - longest n-gram to consider");
        System.out.println("    minRatio - minimal ratio of times n-gram appears over number of lines in n-grams file");
        System.out.println("    minPMI - minimum PMI score n-gram must achieve to be included\n");

        System.out.println("create - to create a new PMI lexicon");
        System.out.println("    ngrams - path to file containing n-grams (JSON formatted list of n-grams)");
        System.out.println("    dataset - path to file containing tagged tweets");
        System.out.println("    lexicon - path to file where the lexicon will be written");
        System.out.println("    options - path to file with classifier options containing stop words and intensifiers");
        System.out.println("    maxError - maximum Z-test error rate for n-gram to be included in lexicon");
        System.out.println("    minSentiment - minimum sentiment value, before normalization, to be included in lexicon");
    }

    private static void generateNGrams(File input, File output, int nGramRange, double cutoffFrequency, double PMIValueThreshold) throws IOException {
        TweetNGramsPMI tweetNGrams = new TweetNGramsPMI();
        ProgressBar.trackProgress(tweetNGrams, "Generating tweet n-grams...");
        List<String> ngrams = tweetNGrams.getFrequentNGrams(new LineReader(input), nGramRange, cutoffFrequency, PMIValueThreshold, N_GRAM_FILTERS);

        JSONUtils.toJSONFile(output, ngrams, true);
    }

    public static void createLexicon(File nGramsFile, File dataSetFile, File lexiconFile, double maxErrorRate, double sentimentValueThreshold) throws IOException {
        Set<String> frequentNGrams = JSONUtils.fromJSONFile(nGramsFile, new TypeToken<Set<String>>(){});
        DataSetReader dataSetReader = new DataSetReader(dataSetFile, 1, 0);

        LexiconCreator lexiconCreator = new LexiconCreator();
        ProgressBar.trackProgress(lexiconCreator, "Creating lexicon...");
        Map<String, Double> lexicon = lexiconCreator.createLexicon(dataSetReader, frequentNGrams, maxErrorRate, sentimentValueThreshold, TWEET_FILTERS);
        JSONUtils.toJSONFile(lexiconFile, MapUtils.sortMapByValue(lexicon), true);
    }
}
