package com.freva.masteroppgave.utils;

import java.io.File;

public class Resources {
    public static final File AFINN_LEXICON = new File("res/data/afinn111.json");
    public static final File PMI_LEXICON = new File("res/tweets/pmilexicon.txt");

    public static final File SPECIAL_WORDS = new File("res/data/options.pmi.json");

    public static final File SEMEVAL_2013_TEST = new File("res/semeval/2013-2-test-gold-B.tsv");
    public static final File SEMEVAL_2013_TRAIN = new File("res/semeval/2013-2-train-full-B.tsv");
    public static final File SEMEVAL_2013_DEV = new File("res/semeval/2013-2-dev-gold-B.tsv");
    public static final File SEMEVAL_2013_NTNU = new File("res/semeval/2013-2-ntnu-gold-B.tsv");
    public static final File SEMEVAL_2014_TEST = new File("res/semeval/2014-9-test-gold-B.tsv");
    public static final File SEMEVAL_2015_TEST = new File("res/semeval/2015-10-test-gold-B.tsv");
    public static final File SEMEVAL_2016_TEST = new File("res/semeval/2016-4-test-gold-A.tsv");
}
