package com.freva.masteroppgave.utils;

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
     * Writes String to file. If file contained anything before operation, it will be emptied first.
     * @param data String to write to file
     * @throws IOException
     */
    public static void writeToFile(File file, String data) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(data);
        }
    }

}
