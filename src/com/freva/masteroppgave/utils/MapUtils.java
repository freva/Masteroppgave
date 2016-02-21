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


    /**
     * Efficient method to extract N largest entries by value form a map
     * @param map Map to extract entries from
     * @param n Number of entries to extract
     * @return Map with n largest entries.
     */
    public static<K, V extends Comparable<V>> Map<K, V> getNLargest(Map<K, V> map, int n) {
        Comparator<Map.Entry<K, V>> comparator = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
        PriorityQueue<Map.Entry<K, V>> largest = new PriorityQueue<>(n, comparator);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            largest.offer(entry);
            while (largest.size() > n) {
                largest.poll();
            }
        }

        Map<K, V> result = new HashMap<>();
        while (largest.size() > 0) {
            Map.Entry<K, V> entry = largest.poll();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    /**
     * Increments value of key by increment if present in the list, otherwise initializes the value to increment.
     * @param map Map to increment key for
     * @param key Key to increment
     * @param increment Value to increment by
     */
    public static<T> void incrementMapByValue(Map<T, Integer> map, T key, int increment) {
        if(! map.containsKey(key)) {
            map.put(key, increment);
        } else {
            map.put(key, map.get(key) + increment);
        }
    }


    /**
     * Performs linear normalization of all values in Map between normMin and normMax
     * @param map Map to normalize values for
     * @param normMin Smallest normalized value
     * @param normMax Largest normalized value
     * @return A new map with double values within [normMin, normMax]
     */
    public static<K, V extends Number & Comparable<V>> Map<K, Double> normalizeMapBetween(Map<K, V> map, double normMin, double normMax) {
        if(map.size() < 2) return new HashMap<>();
        Collection<V> values = new ArrayList<>(map.values());

        double normRange = normMax-normMin;
        double mapMin = Collections.min(values).doubleValue();
        double mapRange = Collections.max(values).doubleValue() - mapMin;
        double rangeFactor = normRange/mapRange;

        Map<K, Double> normalizedMap = new HashMap<>();
        for(Map.Entry<K, V> entry: map.entrySet()) {
            normalizedMap.put(entry.getKey(), normMin + (entry.getValue().doubleValue()-mapMin) * rangeFactor);
        }

        return normalizedMap;
    }


    /**
     * Removes elements from map that are strictly smaller than the threshold element
     * @param map Map to remove items from
     * @param thresh Threshold element
     */
    public static<K, V extends Comparable<V>> void removeInfrequentItems(Map<K, V> map, V thresh) {
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (entry.getValue().compareTo(thresh) < 0) {
                iter.remove();
            }
        }
    }
}
