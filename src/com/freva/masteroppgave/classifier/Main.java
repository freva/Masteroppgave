package com.freva.masteroppgave.classifier;


import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.utils.tools.ClassificationMetrics;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final File semeval_file = new File("res/semeval/2013-2-test-gold-B.tsv");
    private static final File lexicon_file = new File("res/tweets/lexicon.txt");

    public static void main(String[] args) throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(lexicon_file);
        TweetReader tweetReader = new TweetReader(semeval_file);
        Classifier classifier = new Classifier(priorPolarityLexicon,
                Filters::HTMLUnescape, Filters::removeUnicodeEmoticons, Filters::normalizeForm, Filters::removeURL,
                Filters::removeRTTag, Filters::removeHashtag, Filters::removeUsername, Filters::removeEmoticons,
                Filters::removeInnerWordCharacters, Filters::removeNonAlphanumericalText, Filters::removeFreeDigits,
                Filters::removeRepeatedWhitespace, String::trim, String::toLowerCase);


        ClassificationMetrics classificationMetrics = new ClassificationMetrics(DataSetEntry.Class.values());
        while (tweetReader.hasNext()) {
            DataSetEntry entry = tweetReader.readAndPreprocessNextDataSetEntry(3, 2);

            DataSetEntry.Class predicted = classifier.classify(entry.getTweet());
            classificationMetrics.updateEvidence(entry.getClassification(), predicted);
        }

        System.out.println(classificationMetrics.getClassificationReport());
    }
}
