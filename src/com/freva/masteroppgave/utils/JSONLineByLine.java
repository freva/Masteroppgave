package com.freva.masteroppgave.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Scanner;

public class JSONLineByLine<K, V> {
    private Scanner scanner;

    public JSONLineByLine(String filename) throws FileNotFoundException {
        scanner = new Scanner(new File(filename));
    }


    public boolean hasNext() {
        return scanner.hasNext();
    }

    public Map.Entry<K, V> next() throws JSONException {
        JSONObject json = new JSONObject(scanner.nextLine());
        Object key = json.keys().next();
        V value = (V) json.get(String.valueOf(key));
        return new AbstractMap.SimpleEntry<K, V>((K) key, value);
    }
}
