package com.freva.masteroppgave;

import info.debatty.java.lsh.LSHSuperBit;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LSHCosineSimilarity {
    private static final int numberOfStages = 4;
    private static final int bucketSize = 800;

    public static Map<Point, Double> getAllToAllCosineSimilarity(double[][] vectors) throws Exception {
        List<Integer>[] clusters = getSimilarityClusters(vectors);

        double[] rowSqSum = Arrays.stream(vectors)
                .mapToDouble(row -> Math.sqrt(cross(row, row)))
                .toArray();

        Map<Point, Double> cosines = new HashMap<>();
        for(List<Integer> cluster: clusters) {
            if(cluster == null) continue;

            for (int i1 = 1; i1 < cluster.size(); i1++) {
                for (int i2 = 0; i2 < i1; i2++) {
                    int o1 = cluster.get(i1), o2 = cluster.get(i2);
                    Point p = new Point(o1, o2);
                    if(cosines.containsKey(p)) continue;

                    double mul = cross(vectors[o1], vectors[o2]);
                    double cosine = mul / (rowSqSum[o1] * rowSqSum[o2]);
                    cosines.put(p, Double.isNaN(cosine) ? 0 : cosine);
                }
            }
        }

        return cosines;
    }

    private static double cross(double[] a, double[] b) {
        double sum = 0;
        for(int i=0; i<a.length; i++) {
            if(a[i] == 0 || b[i] == 0) continue;
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static List<Integer>[] getSimilarityClusters(double[][] vectors) throws Exception {
        int numberOfBuckets = vectors.length/bucketSize;
        LSHSuperBit lsh = new LSHSuperBit(numberOfStages, numberOfBuckets, vectors[0].length);

        List<Integer>[][] hashes = (ArrayList<Integer>[][]) new ArrayList[numberOfStages][numberOfBuckets];
        for(int i=0; i<vectors.length; i++) {
            int[] hash = lsh.hash(vectors[i]);
            for(int j=0; j<hash.length; j++) {
                if(hashes[j][hash[j]] == null) hashes[j][hash[j]] = new ArrayList<>(bucketSize);
                hashes[j][hash[j]].add(i);
            }
        }

        List<Integer>[] flatHashes = (ArrayList<Integer>[]) new ArrayList[numberOfStages*numberOfBuckets];
        for(int i=0, offset=0; i<hashes.length; offset += hashes[i++].length) {
            System.arraycopy(hashes[i], 0, flatHashes, offset, hashes[i].length);
        }

        return flatHashes;
    }
}
