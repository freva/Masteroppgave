package com.freva.masteroppgave;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.statistics.ClassificationThreshold;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.CharacterCleaner;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.MapUtils;
import com.freva.masteroppgave.utils.Resources;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.reader.LineReader;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Test {
    public static final List<Function<String, String>> filters = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::removeHashtag, Filters::removeEMail, Filters::removeUsername,
            Filters::removeFreeDigits, Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText,
            String::trim, String::toLowerCase);
    private static final String format = "%5.2f";


    public static void main(String[] args) throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.PMI_LEXICON);
        DataSetReader dataSetReader = new DataSetReader(Resources.SEMEVAL_2013_TRAIN, 3, 2);
        Classifier classifier = new Classifier(priorPolarityLexicon, LexicalClassifier.CLASSIFIER_FILTERS);
        ClassificationThreshold threshold = new ClassificationThreshold();

        Parallel.For(dataSetReader, entry -> {
            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            threshold.updateEvidence(entry.getClassification(), predictedSentiment);
        });

        int neutralLinks = 0;
        int nonNeutralLinks = 0;
        for(DataSetEntry entry : new DataSetReader(Resources.SEMEVAL_2013_TEST, 3, 2)) {
            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            DataSetEntry.Class predicted = DataSetEntry.Class.classifyFromThresholds(predictedSentiment, threshold.getLowThreshold(), threshold.getHighThreshold());

            if(RegexFilters.TWITTER_URL.matcher(entry.getTweet()).find()) {
                if(entry.getClassification().isNeutral()) {
                    neutralLinks++;
                } else {
                    nonNeutralLinks++;
                }
            }
        }

        System.out.println(neutralLinks + "/" + (neutralLinks+nonNeutralLinks));
    }


    public static void generateClassifiedBasedOnSmileys() throws IOException {
        LineReader lineReader = new LineReader(new File("res/tweets/filtered1.txt"));
        PriorPolarityLexicon polarityLexicon = new PriorPolarityLexicon(Resources.AFINN_LEXICON);
        Classifier classifier = new Classifier(polarityLexicon, LexicalClassifier.CLASSIFIER_FILTERS);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("res/tweets/classified1.txt")))) {
            Parallel.For(lineReader, line -> {
                double predictedSentiment = classifier.calculateSentiment(line);
                try {
                    if(Math.abs(predictedSentiment) >= 10) {
                        writer.write((predictedSentiment < 0 ? "negative" : "positive") + "\t" + line + "\n");
                    } else if(RegexFilters.EMOTICON_POSITIVE.matcher(line).find()) {
                        writer.write("positive\t" + line + "\n");
                    } else if(RegexFilters.EMOTICON_NEGATIVE.matcher(line).find()) {
                        writer.write("negative\t" + line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public static void generateClassified() throws IOException {
        LineReader lineReader = new LineReader(new File("res/tweets/filtered1.txt"));
        PriorPolarityLexicon polarityLexicon = new PriorPolarityLexicon(Resources.PMI_LEXICON);
        Classifier classifier = new Classifier(polarityLexicon, LexicalClassifier.CLASSIFIER_FILTERS);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("res/classified.txt")))) {
            Parallel.For(lineReader, line -> {
                double predictedSentiment = classifier.calculateSentiment(line);

                if(Math.abs(predictedSentiment) >= 10) {
                    try {
                        writer.write(String.format(format, predictedSentiment) + "\t" + line + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}