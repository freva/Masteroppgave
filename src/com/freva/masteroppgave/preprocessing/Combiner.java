package com.freva.masteroppgave.preprocessing;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Combiner {
    public static void main(String[] args) throws IOException {
        final Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("filtered.txt"), "UTF-8"));
        final HashMap<String, Integer> counter = new HashMap<>();
        int lineCounter = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
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
        output.close();
        System.out.println("\rFiltered set written: " + counter.size() + " out of " + lineCounter);


        Iterator<Map.Entry<String, Integer>> iter = counter.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = iter.next();
            if(entry.getValue() < 50) {
                iter.remove();
            }
        }

        Map.Entry<String, Integer>[] sortedMap = counter.entrySet().toArray(new Map.Entry[counter.size()]);
        Arrays.sort(sortedMap, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("frequent.txt"), "utf-8"))) {
            for (Map.Entry<String, Integer> e : sortedMap) {
                writer.write(e.getKey() + " : " + e.getValue() + "\n");
            }
        }
        System.out.println("Written frequent");
    }
}
