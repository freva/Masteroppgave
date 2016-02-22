package com.freva.masteroppgave.utils.similarity;

public class PairSimilarity<T> implements Comparable<PairSimilarity<T>> {
    private T entry1, entry2;
    private double similarity;

    PairSimilarity(T entry1, T entry2, double similarity) {
        this.entry1 = entry1;
        this.entry2 = entry2;
        this.similarity = similarity;
    }

    public T getEntry1() {
        return entry1;
    }

    public T getEntry2() {
        return entry2;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public int compareTo(PairSimilarity<T> o) {
        return (int) Math.signum(o.getSimilarity()-this.getSimilarity());
    }
}