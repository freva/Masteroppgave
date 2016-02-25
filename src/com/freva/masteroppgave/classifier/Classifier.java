package com.freva.masteroppgave.classifier;


import com.freva.masteroppgave.lexicon.container.PhraseTree;
import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.preprocessors.DataSetEntry;

import java.awt.*;
import java.util.Map;
import java.util.function.Function;

public class Classifier {
    private Function<String, String>[] filters;
    private PriorPolarityLexicon lexicon;
    private PhraseTree phraseTree;

    @SafeVarargs
    public Classifier(PriorPolarityLexicon lexicon, Function<String, String>... filters) {
        this.lexicon = lexicon;
        this.filters = filters;
        this.phraseTree = new PhraseTree(lexicon.getSubjectiveWords());
    }

    public DataSetEntry.Class classify(String tweet) {
        tweet = Filters.chain(tweet, filters);

        for(String sentence: RegexFilters.SENTENCE_END_PUNCTUATION.split(tweet)) {
            Map<Point, String> ngrams = phraseTree.findTrackedWords(sentence);
        }

        return null;
    }
}
