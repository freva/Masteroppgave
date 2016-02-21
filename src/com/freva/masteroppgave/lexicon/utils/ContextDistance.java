package com.freva.masteroppgave.lexicon.utils;

import java.util.*;
import java.util.stream.Collectors;

public class ContextDistance {
    Map<String, Map<String, List<Integer>>> distances = new HashMap<>();

    public ContextDistance() {}

    public ContextDistance(Map<String, Map<String, List<Integer>>> distances) {
        this.distances = distances;
    }

    /**
     * Stores distance between two tokens in an efficient manner. Allows for multiple distances.
     * @param token1 First token
     * @param token2 Second token
     * @param distance Distance between first and the second token
     */
    public void addDistance(String token1, String token2, int distance) {
        if(token1.compareTo(token2) < 0) {
            getDistancesFromTo(token1, token2).add(distance);
        } else {
            getDistancesFromTo(token2, token1).add(-distance);
        }
    }

    public List<Integer> getDistance(String token1, String token2) {
        if(token1.compareTo(token2) < 0) {
            return distances.get(token1).get(token2);
        } else {
            List<Integer> dists = distances.get(token2).get(token1);
            return dists.stream().map(i -> i*(-1)).collect(Collectors.toList());
        }
    }


    private List<Integer> getDistancesFromTo(String key1, String key2) {
        if(! distances.containsKey(key1)) {
            distances.put(key1, new HashMap<>());
        }

        Map<String, List<Integer>> distancesFrom = distances.get(key1);
        if(! distancesFrom.containsKey(key2)) {
            distancesFrom.put(key2, new ArrayList<>());
        }

        return distancesFrom.get(key2);
    }

    public Set<Map.Entry<String, String>> getContextPairs() {
        Set<Map.Entry<String, String>> pairs = new HashSet<>();
        for(Map.Entry<String, Map<String, List<Integer>>> entry: distances.entrySet()) {
            for(String key2: entry.getValue().keySet()) {
                pairs.add(new AbstractMap.SimpleEntry<>(entry.getKey(), key2));
            }
        }

        return pairs;
    }
}
