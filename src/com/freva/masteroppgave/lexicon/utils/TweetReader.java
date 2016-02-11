package com.freva.masteroppgave.lexicon.utils;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.Function;

public class TweetReader {
    @SafeVarargs
    public static String[] readAndPreprocessTweets(String filename, Function<String, String>... filters) throws IOException {
        int totalLines = FileUtils.countLines(filename);
        String[] tweets = new String[totalLines];
        Scanner scanner = new Scanner(new File(filename));

        for (int i=0; scanner.hasNext(); i++) {
            tweets[i] = Filters.chain(scanner.nextLine(), filters);
        }

        return tweets;
    }
}
