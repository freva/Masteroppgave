package com.freva.masteroppgave;

import com.freva.masteroppgave.classifier.Classifier;
import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.freva.masteroppgave.preprocessing.filters.CanonicalForm;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.reader.DataSetReader;
import com.freva.masteroppgave.utils.reader.DataSetReader.Classification;
import com.freva.masteroppgave.statistics.ClassificationCollection;
import com.freva.masteroppgave.utils.tools.Parallel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LexicalClassifier {
    public static final List<Function<String, String>> CLASSIFIER_STRING_FILTERS = Arrays.asList(
            Filters::HTMLUnescape, Filters::parseUnicodeEmojisToAlias, Filters::normalizeForm, Filters::removeURL,
            Filters::removeRTTag, Filters::protectHashtag, Filters::removeEMail, Filters::removeUsername,
            Filters::parseEmoticons, Filters::removeFreeDigits, String::toLowerCase);
    public static final List<Function<String, String>> CLASSIFIER_CHARACTER_FILTERS = Arrays.asList(
            Filters::removeInnerWordCharacters, Filters::removeNonSyntacticalText, CanonicalForm::correctWordViaCanonical);
    public static final Filters CLASSIFIER_FILTERS = new Filters(CLASSIFIER_STRING_FILTERS, CLASSIFIER_CHARACTER_FILTERS);

    private static final Map<String, File> TEST_SETS = new LinkedHashMap<String, File>() {{
        put("2013-TEST", new File("res/semeval/2013-2-test-gold-B.tsv"));
        put("2014-TEST", new File("res/semeval/2014-9-test-gold-B.tsv"));
        put("2015-TEST", new File("res/semeval/2015-10-test-gold-B.tsv"));
        put("2016-TEST", new File("res/semeval/2016-4-test-gold-A.tsv"));
    }};


    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        ClassifierOptions.loadOptions(new File("res/data/options.pmi.json"));

        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(new File("res/data/lexicon.pmi.json"));
        Classifier classifier = new Classifier(priorPolarityLexicon, CLASSIFIER_FILTERS);

        ClassificationCollection classificationCollection = new ClassificationCollection(Classification.values());
        for (Map.Entry<String, File> testSet : TEST_SETS.entrySet()) {
            Parallel.For(new DataSetReader(testSet.getValue(), 3, 2), entry -> {
                Classification predicted = classifier.classify(entry.getTweet());
                classificationCollection.updateEvidence(testSet.getKey(), entry.getClassification(), predicted);
            });
        }

        System.out.println(classificationCollection.getShortClassificationReport());
        System.out.println("In: " + (System.currentTimeMillis() - startTime) + "ms");
    }
}
