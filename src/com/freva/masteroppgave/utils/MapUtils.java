package com.freva.masteroppgave.utils;


import java.util.*;

public class MapUtils {

    /**
     * Sorts Map by value. Map values must implement Comparable.
     * @param map Map to sort
     * @return Sorted map
     */
    public static<K, V extends Comparable<V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<K, V> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
