package com.freva.masteroppgave.preprocessing;


import com.freva.masteroppgave.preprocessing.preprocessors.Combiner;
import com.freva.masteroppgave.preprocessing.preprocessors.GenerateNGrams;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetsTagger;

public class Preprocessor {
    public static void main(String[] args) throws Exception {
        Combiner.rawTweetCleaner("res/tweets/2m.txt", "res/tweets/filtered.txt");
        TweetsTagger.posTagTweets("res/tweets/10k.txt", "res/tweets/tagged.txt", "res/gate_pos_tagger/models/gate-EN-twitter-fast.model");
        GenerateNGrams.createNGrams("res/tweets/10k.txt", 20);
    }
}
