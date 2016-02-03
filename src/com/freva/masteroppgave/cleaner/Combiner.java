package com.freva.masteroppgave.cleaner;

import java.io.*;
import java.util.HashMap;
public class Combiner {

    public static void main(String[] args) throws IOException {
        final Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tweets/filtered.txt"), "UTF-8"));
        final File rawFilesFolder = new File(args[0]);
        final HashMap<String, Integer> counter = new HashMap<>();
        int lineCounter = 0;

        for(File filename: rawFilesFolder.listFiles()) {
            try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
                for(String line; (line = br.readLine()) != null; lineCounter++) {
                    if(lineCounter%100000 == 0) System.out.print("\r" + lineCounter);
                    if(line.startsWith("RT @")) continue;
                    if(line.contains("https://") || line.contains("http://")) continue;
                    if(line.startsWith("Get Weather Updates from The Weather Channel")) continue;

                    if(counter.containsKey(line)) {
                        counter.put(line, counter.get(line) + 1);
                    } else {
                        counter.put(line, 1);
                        output.write(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        output.close();
        System.out.println("\rFiltered set written: " + counter.size() + " out of " + lineCounter);
    }
}
