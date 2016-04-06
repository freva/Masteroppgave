package com.freva.masteroppgave;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.statistics.ClassificationThreshold;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.CharacterCleaner;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.statistics.ClassificationCollection;
import com.freva.masteroppgave.utils.tools.Parallel;
import com.freva.masteroppgave.utils.Resources;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class LexicalClassifier {
    public static final List<Function<String, String>> CLASSIFIER_STRING_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, CharacterCleaner::unicodeEmotesToAlias, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::protectHashtag, Filters::removeEMail, Filters::removeUsername,
            Filters::replaceEmoticons, Filters::removeFreeDigits, String::toLowerCase);
    public static final List<Function<String, String>> CLASSIFIER_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalTextPlus);
    public static final Filters CLASSIFIER_FILTERS = new Filters(CLASSIFIER_STRING_FILTERS, CLASSIFIER_CHARACTER_FILTERS);

    private static final Map<String, File> TEST_SETS = new LinkedHashMap<String, File>(){{
        put("2013-TEST", Resources.SEMEVAL_2013_TEST);
        put("2014-TEST", Resources.SEMEVAL_2014_TEST);
        put("2015-TEST", Resources.SEMEVAL_2015_TEST);
        put("2016-TEST", Resources.SEMEVAL_2016_TEST);
    }};


    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.AFINN_LEXICON);
        DataSetReader dataSetReader = new DataSetReader(Resources.SEMEVAL_2013_TRAIN, 3, 2);
        Classifier classifier = new Classifier(priorPolarityLexicon, CLASSIFIER_FILTERS);
        ClassificationThreshold threshold = new ClassificationThreshold();

        Parallel.For(dataSetReader, entry -> {
            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            threshold.updateEvidence(entry.getClassification(), predictedSentiment);
        });

        System.out.println("Performing tests with threshold: [" + String.format("%4.2f", threshold.getLowThreshold()) + ", " +
                String.format("%4.2f", threshold.getHighThreshold()) + "] with accuracy: " + threshold.getMaxAccuracy() + "\n");

        ClassificationCollection classificationCollection = new ClassificationCollection(DataSetEntry.Class.values());
        for(Map.Entry<String, File> testSet : TEST_SETS.entrySet()) {
            Parallel.For(new DataSetReader(testSet.getValue(), 3, 2), entry -> {
                double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
                DataSetEntry.Class predicted = DataSetEntry.Class.classifyFromThresholds(predictedSentiment, threshold.getLowThreshold(), threshold.getHighThreshold());
                classificationCollection.updateEvidence(testSet.getKey(), entry.getClassification(), predicted);
            });
        }

        System.out.println(classificationCollection.getShortClassificationReport());
        System.out.println("In: " + (System.currentTimeMillis()-startTime) + "ms");
    }
}
