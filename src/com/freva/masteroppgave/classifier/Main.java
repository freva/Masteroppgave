package com.freva.masteroppgave.classifier;


import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetReader;
import com.freva.masteroppgave.utils.Resources;
import com.freva.masteroppgave.utils.tools.ClassificationMetrics;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        PriorPolarityLexicon priorPolarityLexicon = new PriorPolarityLexicon(Resources.OUR_LEXICON);
        TweetReader tweetReader = new TweetReader(Resources.SEMEVAL_2013_TEST);
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
        System.out.println(classificationMetrics.getNormalizedConfusionMatrixReport());
    }
}
