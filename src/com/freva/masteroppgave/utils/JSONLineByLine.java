package com.freva.masteroppgave.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.*;

public class JSONLineByLine<T> {
    private Scanner scanner;
    private Type type;

    /**
     * Class to read JSON strings line by line, where each line contains a <T> entry.
     * @param filename File path to line separated JSON file
     * @throws FileNotFoundException
     */
    public JSONLineByLine(String filename, Type type) throws FileNotFoundException {
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


    public T next() throws JSONException {
        return new Gson().fromJson(scanner.nextLine(), type);
    }
}
