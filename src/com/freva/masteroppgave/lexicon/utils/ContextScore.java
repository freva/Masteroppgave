package com.freva.masteroppgave.lexicon.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.*;

public class ContextScore implements JsonSerializer<ContextScore> {
    private Map<String, Map<String, Score>> scores = new HashMap<>();

    public ContextScore() {}

    public ContextScore(Map<String, Map<String, Score>> scores) {
        this.scores = scores;
    }

    /**
     * Stores distance between two tokens in an efficient manner.
     * @param token1 First token
     * @param token2 Second token
     * @param score Score between first and the second token
     */
    public void addDistance(String token1, String token2, int score) {
        if(token1.compareTo(token2) < 0) {
            updateScores(token1, token2, score);
        } else {
            updateScores(token2, token1, -score);
        }
    }

    public int getScore(String token1, String token2) {
        if(token1.compareTo(token2) < 0) {
            return scores.get(token1).get(token2).getRightScore();
        } else {
            return scores.get(token2).get(token1).getLeftScore();
        }
    }


    private void updateScores(String key1, String key2, int score) {
        if(! scores.containsKey(key1)) {
            scores.put(key1, new HashMap<>());
        }

        Map<String, Score> distancesFrom = scores.get(key1);
        if(! distancesFrom.containsKey(key2)) {
            distancesFrom.put(key2, new Score(0));
        }

        distancesFrom.get(key2).incrementScore(score);
    }

    public Set<Map.Entry<String, String>> getContextPairs() {
        Set<Map.Entry<String, String>> pairs = new HashSet<>();
        for(Map.Entry<String, Map<String, Score>> entry: scores.entrySet()) {
            for(String key2: entry.getValue().keySet()) {
                pairs.add(new AbstractMap.SimpleEntry<>(entry.getKey(), key2));
            }
        }

        return pairs;
    }

    @Override
    public JsonElement serialize(ContextScore contextScore, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject jsonObject = new JsonObject();

        for(Map.Entry<String, Map<String, Score>> entry1: scores.entrySet()) {
            JsonObject temp = new JsonObject();
            for(Map.Entry<String, Score> entry2: entry1.getValue().entrySet()) {
                temp.addProperty(entry2.getKey(), entry2.getValue().score);
            }
            jsonObject.add(entry1.getKey(), temp);
        }
        return jsonObject;
    }

    private class Score {
        private static final int field_length = 4;
        private static final int field_mask = 0xF;
        int score;

        public Score(int score) {
            this.score = score;
        }

        public void incrementScore(int increment) {
            if(increment > 0) {
                score += increment;
            } else {
                score += (-increment)<<field_length;
            }
        }

        public int getLeftScore() {
            return (score>>field_length) & field_mask;
        }

        public int getRightScore() {
            return score & field_mask;
        }
    }
}
