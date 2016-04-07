package com.freva.masteroppgave;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.statistics.ClassificationOptimizer;
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
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.AFINN_LEXICON);
        Classifier classifier = new Classifier(priorPolarityLexicon);

        DataSetReader dataSetReader = new DataSetReader(Resources.SEMEVAL_2013_TRAIN, 3, 2);
        List<DataSetEntry> entries = new ArrayList<>();
        dataSetReader.forEach(entries::add);
        entries.forEach(e -> e.applyFilters(LexicalClassifier.CLASSIFIER_FILTERS));

        final long startTime = System.currentTimeMillis();
        ClassificationOptimizer.runOptimizer(classifier, entries);
        System.out.println(System.currentTimeMillis()-startTime);
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
