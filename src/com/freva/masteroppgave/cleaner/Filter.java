package com.freva.masteroppgave.cleaner;


import java.io.*;

public class Filter {
    public static void main(String[] args) throws Exception {
        GatePosTagger tagger = new GatePosTagger("models/gate-EN-twitter-fast.model");
        String input_filename = "res/tweets/10k.txt";
        String output_filename = "res/tweets/tagged.txt";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_filename), "windows-1252"))) {
            try(BufferedReader br = new BufferedReader(new FileReader(input_filename))) {
                for(String line; (line = br.readLine()) != null; ) {
                    writer.write(tagger.tagSentence(line) + "\n");
                }
            }
        }
    }
}
