package com.freva.masteroppgave.lexicon.graph;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import com.freva.masteroppgave.preprocessing.filters.WordFilters;
import com.freva.masteroppgave.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Node implements Comparable<Node> {
    private static final Pattern punctuation = Pattern.compile("[!?.]");
    private static final int phraseVectorSize = 10;
    private static final int phraseWindowSize = 6;

    private HashMap<String, Integer> rightSideContextWords = new HashMap<>();
    private HashMap<String, Integer> leftSideContextWords = new HashMap<>();
    private HashMap<String, Double> posValues = new HashMap<>();
    private HashMap<String, Double> negValues = new HashMap<>();
    private ArrayList<Edge> neighbors = new ArrayList<>();

    private ContextWords contextWordsCache;
    private boolean cacheUpToDate = false;

    private String[] phraseWords;
    private String phrase;
    private double currentScore;

    public Node(String phrase) {
        this.phrase = phrase;
        this.phraseWords = RegexFilters.WHITESPACE.split(phrase);
    }


    /**
     * Finds all occurrences of the phrase withing the given tweet and creates two Strings(phraseWindows).
     * The phraseWindows contains the x = phraseWindowSize words in front of the phrase and the x words following the phrase respectively.
     * Ex: Tweet = "I really don't like that guy", Phrase = "don't like", PhraseWindowSize = 2  -> phraseWindows = ["I really don't like", "don't like that guy"]
     * @param context - The tweet the phrase occurs in.
     */
    public void updatePhraseContext(String context) {
        String[] contextWords = RegexFilters.WHITESPACE.split(context);

        ArrayList<Integer> indexes = new ArrayList<>();
        for (int j = 0; j < contextWords.length; j++) {
            if (matchesAtIndex(contextWords, j)) {
                indexes.add(j);
            }
        }

        for(Integer phraseStart: indexes) {
            for (int i = phraseStart + phraseWords.length - 1; i >= Math.max(0, phraseStart - phraseWindowSize); i--) {
                if (punctuation.matcher(contextWords[i]).find()) {
                    break;
                }

                MapUtils.incrementMapValue(leftSideContextWords, contextWords[i]);
            }


            for (int i = phraseStart; i < Math.min(contextWords.length-1, phraseStart+phraseWords.length+phraseWindowSize); i++) {
                if(punctuation.matcher(contextWords[i]).find()) {
                    String wordWithoutPunctuation = punctuation.matcher(contextWords[i]).replaceAll("");
                    MapUtils.incrementMapValue(rightSideContextWords, wordWithoutPunctuation);
                    break;
                }

                MapUtils.incrementMapValue(rightSideContextWords, contextWords[i]);
            }
        }

        cacheUpToDate = false;
    }

    /**
     * Returns the phrase associated with the node.
     * @return phrase
     */
    public String getPhrase() {
        return phrase;
    }


    public ContextWords getContextWords() {
        if(! cacheUpToDate) {
            String[] leftSideContext = getFrequentContextWords(leftSideContextWords);
            String[] rightSideContext = getFrequentContextWords(rightSideContextWords);
            contextWordsCache = new ContextWords(leftSideContext, rightSideContext);
            cacheUpToDate = true;
        }

        return contextWordsCache;
    }


    private String[] getFrequentContextWords(Map<String, Integer> map) {
        String[] frequentContextWords = new String[phraseVectorSize];
        Map<String, Integer> sortedWordFrequency = MapUtils.sortMapByValue(map);
        int counter = 0;
        for (String sortedKey: sortedWordFrequency.keySet()) {
            if(WordFilters.containsStopWord(sortedKey)) continue;
            if (counter < phraseVectorSize) {
                frequentContextWords[counter++] = sortedKey;
            } else {
                break;
            }
        }
        return frequentContextWords;
    }

    /**
     * Returns the nodes neighbors
     * @return neighbors
     */
    public ArrayList<Edge> getNeighbors() {
        return neighbors;
    }

    /**
     * Adds a neighbor node to the array of neighbors
     * @param neighbor node
     */
    public void addNeighbor(Edge neighbor) {
        neighbors.add(neighbor);
        posValues.put(neighbor.getNeighbor().getPhrase(), 0.0);
    }

    /**
     * Updates the current propagation score, as well as either the max positive score or the min negative score propagated from the given neighbor node.
     * @param score - The score propagated from the given neighbor.(Can be positive or negative)
     * @param neighbor - The neighbor node propagating the sentiment score to the current node.
     */
    public void updateSentimentScore(double score, Node neighbor) {
        currentScore = score;
        if(score < 0) {
            updateNegScore(score, neighbor);
        }
        else updatePosScore(score, neighbor);
    }

    /**
     * Updates the max positive sentiment score propagated from the given neighbor if the given score is bigger than the previous max score propagated from the neighbor.
     * @param score - The positive sentiment score propagated from the neighbor.
     * @param neighbor - The neighbor node propagating the sentiment score.
     */
    private void updatePosScore(double score, Node neighbor) {
        if(!posValues.containsKey(neighbor.getPhrase())) {
            posValues.put(neighbor.getPhrase(), score);
        }
        else {
            double maxScore = Math.max(posValues.get(neighbor.getPhrase()), score);
            posValues.put(neighbor.getPhrase(), maxScore);
        }
    }

    /**
     * Updates the min negative sentiment score propagated from the given neighbor if the given score is smaller than the previous min score propagated from the neighbor.
     * @param score - The negative sentiment score propagated from the neighbor.
     * @param neighbor - The neighbor node propagating the sentiment score.
     */
    private void updateNegScore(double score, Node neighbor) {
        if(!negValues.containsKey(neighbor.getPhrase())) {
            negValues.put(neighbor.getPhrase(), score);
        }
        else {
            double minScore = Math.min(negValues.get(neighbor.getPhrase()), score);
            negValues.put(neighbor.getPhrase(), minScore);
        }
    }

    /**
     * Returns the overall sentiment score of the node.
     * @return - Sentiment score.
     */
    public double getSentimentScore() {
        return sumScores(posValues) + (sumScores(negValues));
    }

    /**
     * Returns the current propagation score. The score that is going to be propagated out from the node to its neighbors.
     * @return - Current propagation score.
     */
    public double getCurrentScore(){
        return currentScore;
    }

    /**
     * Sums up the sentiment scores contained in a HashMap. This method is called once for the HashMaps containing positive and negative sentiment scores respectively.
     * @param scores - HashMap containing sentiment scores
     * @return - The sum of the sentiment scores.
     */
    private double sumScores(HashMap<String, Double> scores) {
        double total = 0.0;
        for(Double score : scores.values()) {
            total += score;
        }
        return total;
    }

    @Override
    public int compareTo(Node other) {
        if(other == null) return -1;
        else return (int) Math.signum(other.getSentimentScore()-this.getSentimentScore());
    }



    /**
     * Checks if a given index is the starting index of a phrase in a contextWords.
     * @param contextWords - The given contextWords.
     * @param index - The current index.
     * @return True if correct index, False else.
     */
    private boolean matchesAtIndex(String[] contextWords, int index) {
        if(index+phraseWords.length > contextWords.length) return false;

        for(int j = 0 ; j < phraseWords.length && index+j < contextWords.length; j++) {
            if(! contextWords[index+j].equals(phraseWords[j])) {
                return false;
            }
        }
        return true;
    }

}
