package com.freva.masteroppgave;


import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.classifier.Threshold;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.utils.Resources;
import com.freva.masteroppgave.utils.tools.ClassificationMetrics;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class LexicalClassifier {
    public static final List<Function<String, String>> filters = Arrays.asList(
            Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::hashtagToWord, Filters::removeUsername, Filters::replaceEmoticons,
            Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
            Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);

    private static final double neutralLowThreshold = -0.45;
    private static final double neutralHighThreshold = 0.65;


    public static void main(String[] args) throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.OUR_LEXICON);
        TweetReader tweetReader = new TweetReader(Resources.SEMEVAL_2013_TEST);
        Classifier classifier = new Classifier(priorPolarityLexicon, filters);
        Threshold threshold = new Threshold();

        ClassificationMetrics classificationMetrics = new ClassificationMetrics(DataSetEntry.Class.values());
        while (tweetReader.hasNext()) {
            DataSetEntry entry = tweetReader.readAndPreprocessNextDataSetEntry(3, 2);

            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            DataSetEntry.Class predicted = DataSetEntry.Class.classifyFromThresholds(predictedSentiment, neutralLowThreshold, neutralHighThreshold);

            classificationMetrics.updateEvidence(entry.getClassification(), predicted);
            threshold.updateEvidence(entry.getClassification(), predictedSentiment);
        }

        System.out.println(classificationMetrics.getClassificationReport());
        System.out.println(classificationMetrics.getNormalizedConfusionMatrixReport());
        System.out.println("Current thresholds: [" + neutralLowThreshold + ", " + neutralHighThreshold +
                "] with accuracy: " + classificationMetrics.getAccuracy());
        System.out.println("Optimal thresholds: [" + String.format("%4.2f", threshold.getLowThreshold()) + ", " +
                String.format("%4.2f", threshold.getHighThreshold()) + "] with accuracy: " + threshold.getMaxAccuracy());
    }
}
