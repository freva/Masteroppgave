package com.freva.masteroppgave.preprocessing.preprocessors;

import java.io.*;
import java.util.HashSet;

public class Combiner {
    public static void rawTweetCleaner(String input_filename, String output_filename) throws IOException {
        final HashSet<String> unique = new HashSet<>();
        int lineCounter = 0;

        try(Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "UTF-8"))) {
            try (BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
                for (String line; (line = br.readLine()) != null; lineCounter++) {
                    if (lineCounter % 100000 == 0) System.out.print("\r" + lineCounter);
                    if (! shouldInclude(line) || unique.contains(line)) continue;

                    unique.add(line.toLowerCase());
                    output.write(line + "\n");
                }
            }
        }
    }


    private static boolean shouldInclude(String text) {
        if (text.startsWith("RT @")) return false;
        if (text.contains("https://") || text.contains("http://")) return false;
        if (text.startsWith("Get Weather Updates from The Weather Channel")) return false;

        return true;
    }
}
