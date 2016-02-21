package com.freva.masteroppgave.lexicon.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextDistance {
    Map<String, Map<String, List<Integer>>> distances = new HashMap<>();

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
}
