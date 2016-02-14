package com.freva.masteroppgave.lexicon.utils;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.Function;

public class TweetReader implements Progressable {
    private int totalLines = 0;
    private int lineCounter = 0;

    @SafeVarargs
    public final String[] readAndPreprocessTweets(String filename, Function<String, String>... filters) throws IOException {
        this.totalLines = FileUtils.countLines(filename);
        String[] tweets = new String[totalLines];
        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNext()) {
            tweets[lineCounter++] = Filters.chain(scanner.nextLine(), filters);
        }

        return tweets;
    }

    @Override
    public double getProgress() {
        return (totalLines == 0 ? 0 : 100.0*lineCounter/totalLines);
    }
}
