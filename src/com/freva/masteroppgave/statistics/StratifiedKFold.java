package com.freva.masteroppgave.statistics;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StratifiedKFold {
    private final Classifier classifier;
    private final List<DataSetEntry> entries;
    private final int k;

    public StratifiedKFold(Classifier classifier, List<DataSetEntry> entries, int k) {
        Collections.shuffle(entries);
        this.classifier = classifier;
        this.entries = entries;
        this.k = k;
    }

    public double calculateScore() {
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

    private double trainAndTest(List<DataSetEntry> trainSet, List<DataSetEntry> testSet) {
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
