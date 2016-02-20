package com.freva.masteroppgave.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.Files;


public class FileUtils {
    /**
     * Calculates number of lines in a file
     * @param file File to count lines in
     * @return Number of lines in a file
     * @throws IOException
     */
    public static int countLines(File file) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        }
    }


    /**
     * Reads entire file into a String
     * @param file File to read in
     * @return String with entire file contents
     * @throws IOException
     */
    public static String readEntireFileIntoString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }


    /**
     * Writes object to file in JSON format
     * @param obj Object to write to file
     * @param file File to write to
     * @param pretty Use pretty JSON formatting
     * @throws IOException
     */
    public static void writeObjectToFileAsJSON(Object obj, File file, boolean pretty) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            Gson gson = (pretty ? new GsonBuilder().setPrettyPrinting() : new GsonBuilder()).create();
            gson.toJson(obj, writer);
        }
    }


    /**
     * Reads in object from JSON formatted file
     * @param file File containing JSON formatted object
     * @param typeToken Type of object stored in file
     * @return Object of type specified by typeToken
     * @throws IOException
     */
    public static<T> T readObjectFromJSONFile(File file, TypeToken<T> typeToken) throws IOException {
        String json = FileUtils.readEntireFileIntoString(file);
        return new Gson().fromJson(json, typeToken.getType());
    }
}
