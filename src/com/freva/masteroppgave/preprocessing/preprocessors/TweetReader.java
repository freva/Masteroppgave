package com.freva.masteroppgave.preprocessing.preprocessors;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.Function;

public class TweetReader implements Progressable {
    private Function<String, String>[] filters;
    private Scanner scanner;
    private int totalLines = 0;
    private int lineCounter = 0;

    @SafeVarargs
    public TweetReader(File file, Function<String, String>... filters) throws IOException {
        this.totalLines = FileUtils.countLines(file);
        this.scanner = new Scanner(file);
        this.filters = filters;
    }


    /**
     * Reads and preprocesses all tweets at once
     * @return Returns String array with all preprocessed tweets in file
     * @throws IOException
     */
    public final String[] readAndPreprocessAllTweets() throws IOException {
        String[] tweets = new String[totalLines];

        while (scanner.hasNext()) {
            tweets[lineCounter++] = Filters.chain(scanner.nextLine(), filters);
        }

        return tweets;
    }


    /**
     * Reads and preprocesses next tweet
     * @return Preprocessed tweet in a String
     */
    public String readAndPreprocessNextTweet() {
        lineCounter++;
        return Filters.chain(scanner.nextLine(), filters);
    }


    /**
     * Check if there are more entries left
     * @return True if more entries present
     */
    public boolean hasNext() {
        return scanner.hasNext();
    }

    @Override
    public double getProgress() {
        return (totalLines == 0 ? 0 : 100.0*lineCounter/totalLines);
    }
}
