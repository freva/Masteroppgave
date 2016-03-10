package com.freva.masteroppgave.utils.reader;

import com.freva.masteroppgave.utils.JSONUtils;
import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JSONLineByLine<T> implements Iterator<T>, Iterable<T>, Progressable {
    private LineReader lineReader;
    private TypeToken<T> type;

    /**
     * Class to read JSON strings line by line, where each line contains a <T> entry.
     * @param file File containing line separated JSON objects
     * @throws IOException
     */
    public JSONLineByLine(File file, TypeToken<T> type) throws IOException {
        this.lineReader = new LineReader(file);
        this.type = type;
    }


    /**
     * Check if there are more entries left
     * @return True if more entries present
     */
    public boolean hasNext() {
        return lineReader.hasNext();
    }


    /**
     * Reads and parses the next JSON entry from file
     * @return Parsed element of type <T>
     * @throws JSONException
     */
    public T next() throws JSONException {
        return JSONUtils.fromJSON(lineReader.next(), type);
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public double getProgress() {
        return lineReader.getProgress();
    }
}
