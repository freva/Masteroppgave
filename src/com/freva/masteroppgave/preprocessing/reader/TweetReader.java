package com.freva.masteroppgave.preprocessing.reader;

import com.freva.masteroppgave.preprocessing.filters.Filters;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class TweetReader extends LineReader {
    private List<Function<String, String>> filters;

    public TweetReader(File file, List<Function<String, String>> filters) throws IOException {
        super(file);
        this.filters = filters;
    }


    /**
     * Reads and preprocesses next tweet
     * @return Preprocessed tweet in a String
     */
    public String next() {
        return filters == null ? super.next() : Filters.chain(super.next(), filters);
    }
}
