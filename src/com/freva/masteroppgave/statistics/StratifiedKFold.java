package com.freva.masteroppgave.statistics;

import com.freva.masteroppgave.LexicalClassifier;
import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.Resources;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StratifiedKFold {
    private final PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.AFINN_LEXICON);
    private final Classifier classifier = new Classifier(priorPolarityLexicon, LexicalClassifier.CLASSIFIER_FILTERS);

    private List<DataSetEntry> entries;
    private int k;


    public StratifiedKFold(List<DataSetEntry> entries, int k) throws IOException {
        Collections.shuffle(entries);
        this.entries = entries;
        this.k = k;
    }

    public double calculateScore() throws IOException {
        int partLength = entries.size() / k;

        double F1Score = 0;
        for(int i = 0 ; i < k ; i++){
            List<DataSetEntry> testSet = new ArrayList<>(entries.subList(i*partLength, (i+1)*partLength));
            List<DataSetEntry> trainSet = new ArrayList<>(entries.subList(0, i*partLength));
            trainSet.addAll(entries.subList((i+1)*partLength, entries.size()));

            F1Score += trainAndTest(trainSet, testSet);
        }
        return F1Score/k;
    }

    private double trainAndTest(List<DataSetEntry> trainSet, List<DataSetEntry> testSet) throws IOException {
        ClassificationThreshold threshold = new ClassificationThreshold();
        Parallel.For(trainSet, entry -> {
            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            threshold.updateEvidence(entry.getClassification(), predictedSentiment);
        });

        ClassificationMetrics classificationMetrics = new ClassificationMetrics(DataSetEntry.Class.values());
        Parallel.For(testSet, entry -> {
            double predictedSentiment = classifier.calculateSentiment(entry.getTweet());
            DataSetEntry.Class predicted = DataSetEntry.Class.classifyFromThresholds(predictedSentiment, threshold.getLowThreshold(), threshold.getHighThreshold());
            classificationMetrics.updateEvidence(entry.getClassification(), predicted);
        });

        return classificationMetrics.getF1Score();
    }
}
