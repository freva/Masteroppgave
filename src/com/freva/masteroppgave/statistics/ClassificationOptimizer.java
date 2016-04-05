package com.freva.masteroppgave.statistics;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassificationOptimizer {
    private static final Map<ClassifierOptions.Variable, double[]> variableValues = new HashMap<>();

    static {
        variableValues.put(ClassifierOptions.Variable.NEGATION_VALUE, new double[]{0.5, 0.8, 1.0, 1.2, 1.5});
        variableValues.put(ClassifierOptions.Variable.EXCLAMATION_INTENSIFIER, new double[]{1.0, 1.5, 2.0, 2.5});
        variableValues.put(ClassifierOptions.Variable.QUESTION_INTENSIFIER, new double[]{0.3, 0.5, 0.7, 1.0});
        variableValues.put(ClassifierOptions.Variable.NEGATION_SCOPE_LENGTH, new double[]{3, 4, 5});
        variableValues.put(ClassifierOptions.Variable.AMPLIFIER_SCALAR, new double[]{1, 2, 2.5, 3, 3.5, 4});
        variableValues.put(ClassifierOptions.Variable.DOWNTONER_SCALAR, new double[]{0.1, 0.2, 0.3, 0.4, 0.6, 0.8, 1});
    }

    public static void runOptimizer(Classifier classifier, List<DataSetEntry> entries) throws IOException {
        final StratifiedKFold stratifiedKFold = new StratifiedKFold(classifier, entries, 3);

        int numChangedVariables;
        do {
            numChangedVariables = 0;
            for (ClassifierOptions.Variable variable : variableValues.keySet()) {
                double currentValue = ClassifierOptions.getVariable(variable);
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
                if (currentValue != bestValue) numChangedVariables++;
            }

            System.out.println(numChangedVariables);
        } while (numChangedVariables > 0);

        FileUtils.writeToFile(new File("res/tweets/test.txt"), ClassifierOptions.getAsJson());
    }
}
