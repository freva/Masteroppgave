package com.freva.masteroppgave.utils;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {
    /**
     * Calculates number of lines in a file
     * @param filename File path
     * @return Number of lines in a file
     * @throws IOException
     */
    public static int countLines(String filename) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
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
     * @param filename File path to file to read in
     * @return String with entire file contents
     * @throws IOException
     */
    public static String readEntireFileIntoString(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }
}
