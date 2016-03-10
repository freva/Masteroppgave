package com.freva.masteroppgave;


import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.classifier.Threshold;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.tools.Parallel;
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

    private static final double neutralLowThreshold = -3.74;
    private static final double neutralHighThreshold = 3.60;


    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.PMI_LEXICON);
        DataSetReader dataSetReader = new DataSetReader(Resources.SEMEVAL_2016_TEST, 3, 2);
        Classifier classifier = new Classifier(priorPolarityLexicon, filters);
        Threshold threshold = new Threshold();

        ClassificationMetrics classificationMetrics = new ClassificationMetrics(DataSetEntry.Class.values());
        Parallel.For(dataSetReader, entry -> {
            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            DataSetEntry.Class predicted = DataSetEntry.Class.classifyFromThresholds(predictedSentiment, neutralLowThreshold, neutralHighThreshold);

            classificationMetrics.updateEvidence(entry.getClassification(), predicted);
            threshold.updateEvidence(entry.getClassification(), predictedSentiment);
        });

        System.out.println(classificationMetrics.getClassificationReport());
        System.out.println(classificationMetrics.getNormalizedConfusionMatrixReport());
        System.out.println("Current thresholds: [" + neutralLowThreshold + ", " + neutralHighThreshold +
                "] with accuracy: " + classificationMetrics.getAccuracy());
        System.out.println("Optimal thresholds: [" + String.format("%4.2f", threshold.getLowThreshold()) + ", " +
                String.format("%4.2f", threshold.getHighThreshold()) + "] with accuracy: " + threshold.getMaxAccuracy());
        System.out.println("In: " + (System.currentTimeMillis()-startTime) + "ms");
    }
}
