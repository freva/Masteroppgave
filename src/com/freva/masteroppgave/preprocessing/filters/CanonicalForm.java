package com.freva.masteroppgave.preprocessing.filters;

import com.freva.masteroppgave.utils.FileUtils;
import com.freva.masteroppgave.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CanonicalForm {
    private static Map<String, Set<String>> dictionary = new HashMap<>();

    public static void loadDictionary(File file) throws IOException {
        dictionary = JSONUtils.fromJSON(FileUtils.readEntireFileIntoString(file), new TypeToken<Map<String, Set<String>>>(){});
    }

    public static String correctWordViaCanonical(String text) {
        String canonical = Filters.removeRepeatingCharacters(text);

        Set<String> candidates = dictionary.get(canonical);
        if(candidates == null) {
            return text;
        } else if(candidates.size() == 1) {
            return candidates.iterator().next();
        }

        int closestDist = Integer.MAX_VALUE;
        String closestString = canonical;
        for(String candidate: candidates) {
            int dist = levenshteinDistance(text, candidate);
            if(dist < closestDist) {
                closestDist = dist;
                closestString = candidate;
            }
        }

        return closestString;
    }


    /**
     * Calculates edit distance between two strings without replacement
     * @param lhs String one
     * @param rhs String two
     * @return Minimum number of insertions/deletions between the two strings to make them equal
     */
    private static int levenshteinDistance(String lhs, String rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        for (int i = 0; i < len0; i++) cost[i] = i;

        for (int j = 1; j < len1; j++) {
            newcost[0] = j;

            for (int i = 1; i < len0; i++) {
                if (lhs.charAt(i - 1) == rhs.charAt(j - 1)) {
                    newcost[i] = cost[i-1];
                } else {
                    int insert = cost[i] + 1;
                    int delete = newcost[i - 1] + 1;
                    newcost[i] = Math.min(insert, delete);
                }
            }

            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        return cost[len0 - 1];
    }
}
