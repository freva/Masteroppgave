package com.freva.masteroppgave.statistics;

import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.Resources;
import com.freva.masteroppgave.utils.reader.DataSetReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassificationOptimizer {
    private static final Map<ClassifierOptions.Variable, double[]> variableValues = new HashMap<>();
    private static final Map<String, double[]> intensifiersValues = new HashMap<>();

    static {
        variableValues.put(ClassifierOptions.Variable.NEGATION_VALUE, new double[]{0.5, 0.8, 1.0, 1.5});
        variableValues.put(ClassifierOptions.Variable.EXCLAMATION_INTENSIFIER, new double[]{1.0, 1.5, 2.0, 2.5});
        variableValues.put(ClassifierOptions.Variable.QUESTION_INTENSIFIER, new double[]{0.3, 0.5, 0.7, 1.0});
        variableValues.put(ClassifierOptions.Variable.NEGATION_SCOPE_LENGTH, new double[]{3, 4, 5});

        intensifiersValues.put("very", new double[]{1.25, 1.5, 2, 2.5});
    }

    public static void runOptimizer(List<DataSetEntry> entries) throws IOException {
        StratifiedKFold stratifiedKFold = new StratifiedKFold(entries, 3);

        for (ClassifierOptions.Variable variable : variableValues.keySet()) {
            double bestValue = 0;
            double bestScore = 0;

            for (double value : variableValues.get(variable)) {
                ClassifierOptions.setVariable(variable, value);
                final double score = stratifiedKFold.calculateScore();

                if (score > bestScore) {
                    bestScore = score;
                    bestValue = value;
                }
            }

            ClassifierOptions.setVariable(variable, bestValue);
        }

        for (String intensifier : intensifiersValues.keySet()) {
            double bestValue = 0;
            double bestScore = 0;

            for (double value : intensifiersValues.get(intensifier)) {
                ClassifierOptions.setIntensifierValue(intensifier, value);
                final double score = stratifiedKFold.calculateScore();

                if (score > bestScore) {
                    bestScore = score;
                    bestValue = value;
                }
            }

            ClassifierOptions.setIntensifierValue(intensifier, bestValue);
        }

        System.out.println(ClassifierOptions.getAsJson());
    }
}
