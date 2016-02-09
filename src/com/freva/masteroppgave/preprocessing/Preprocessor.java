package com.freva.masteroppgave.preprocessing;


import com.freva.masteroppgave.preprocessing.preprocessors.TweetsFilterer;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetsNGrams;
import com.freva.masteroppgave.preprocessing.preprocessors.TweetsTagger;

public class Preprocessor {
    public static void main(String[] args) throws Exception {
        TweetsFilterer.rawTweetCleaner("res/tweets/2m.txt", "res/tweets/filtered.txt");
        TweetsTagger.posTagTweets("res/tweets/10k.txt", "res/tweets/tagged.txt", "res/gate_pos_tagger/models/gate-EN-twitter-fast.model");
        TweetsNGrams.createNGrams("res/tweets/10k.txt", 20);
    }
}
