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


    public TweetReader(File file) throws IOException {
        this.totalLines = FileUtils.countLines(file);
        this.scanner = new Scanner(file);
    }

    @SafeVarargs
    public TweetReader(File file, Function<String, String>... filters) throws IOException {
        this(file);
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
            tweets[lineCounter-1] = readAndPreprocessNextTweet();
        }

        return tweets;
    }


    /**
     * Reads and preprocesses next tweet
     * @return Preprocessed tweet in a String
     */
    public String readAndPreprocessNextTweet() {
        lineCounter++;
        return filters == null ? scanner.nextLine() : Filters.chain(scanner.nextLine(), filters);
    }


    public DataSetEntry readAndPreprocessNextDataSetEntry(int tweetIndex, int classIndex) {
        lineCounter++;
        return new DataSetEntry(scanner.nextLine(), tweetIndex, classIndex);
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
