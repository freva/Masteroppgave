package com.freva.masteroppgave.utils;

import com.freva.masteroppgave.utils.progressbar.Progressable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class JSONLineByLine<T> implements Progressable {
    private int totalLines = 0;
    private int lineCounter = 0;
    private Scanner scanner;
    private Type type;

    /**
     * Class to read JSON strings line by line, where each line contains a <T> entry.
     * @param filename File path to line separated JSON file
     * @throws FileNotFoundException
     */
    public JSONLineByLine(String filename, Type type) throws IOException {
        this.totalLines = FileUtils.countLines(filename);
        this.scanner = new Scanner(new File(filename));
        this.type = type;
    }


    /**
     * Check if there are more entries left
     * @return True if more entries present
     */
    public boolean hasNext() {
        return scanner.hasNext();
    }


    /**
     * Reads and parses the next JSON entry from file
     * @return Parsed element of type <T>
     */
    public T next() throws JSONException {
        lineCounter++;
        return new Gson().fromJson(scanner.nextLine(), type);
    }

    @Override
    public double getProgress() {
        return (totalLines == 0 ? 0 : 100.0*lineCounter/totalLines);
    }
}
