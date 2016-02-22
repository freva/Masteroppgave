package com.freva.masteroppgave.utils.tools;

import java.util.*;

public class FixedPriorityQueue<T> extends PriorityQueue<T> {
    private int maxSize;

    public FixedPriorityQueue(int maxSize) {
        super(maxSize+1);
        this.maxSize = maxSize;
    }

    public FixedPriorityQueue(int maxSize, Comparator<T> comparator) {
        super(maxSize+1, comparator);
        this.maxSize = maxSize;
    }

    public FixedPriorityQueue(int maxSize, Collection<T> elements) {
        this(maxSize);
        this.addAll(elements);
    }

    public FixedPriorityQueue(int maxSize, Comparator<T> comparator, Collection<T> elements) {
        this(maxSize, comparator);
        this.addAll(elements);
    }


    public boolean add(T element) {
        while (size() > maxSize) {
            poll();
        }
        return offer(element);
    }

    public boolean addAll(Collection<? extends T> collection) {
        collection.forEach(this::add);
        return true;
    }


    public int getMaxSize() {
        return maxSize;
    }

    public List<T> sortedItems() {
        List<T> items = new ArrayList<>(size());
        iterator().forEachRemaining(items::add);

        if(comparator() != null) {
            Collections.sort(items, comparator().reversed());
        } else {
            Collections.sort(items, Collections.reverseOrder());
        }

        return items;
    }
}
