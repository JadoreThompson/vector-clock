package com.zenz.vector_clock;

import java.util.Arrays;

public class DistributedVectorClock implements VectorClock {
    private long[] vectorClock;
    private final int id;

    public DistributedVectorClock(int id) {
        this.id = id;
        this.vectorClock = new long[id + 1];
    }

    public DistributedVectorClock(long[] vectorClock, int id) {
        if (id < 0 || id >= vectorClock.length) {
            throw new IllegalArgumentException("Invalid node id");
        }

        this.vectorClock = Arrays.copyOf(vectorClock, vectorClock.length);
        this.id = id;
    }

    public void increment() {
        ensureCapacity(id + 1);
        vectorClock[id]++;
    }

    @Override
    public void merge(VectorClock other) {
        long[] otherClock = other.getClock();
        ensureCapacity(otherClock.length);

        for (int i = 0; i < otherClock.length; i++) {
            vectorClock[i] = Math.max(vectorClock[i], otherClock[i]);
        }

        vectorClock[id]++;
    }

    @Override
    public boolean happensBefore(VectorClock other) {
        long[] otherClock = other.getClock();
        int maxLen = Math.max(vectorClock.length, otherClock.length);

        boolean strictlyLess = false;

        for (int i = 0; i < maxLen; i++) {
            long a = i < vectorClock.length ? vectorClock[i] : 0;
            long b = i < otherClock.length ? otherClock[i] : 0;

            if (a > b) return false;
            if (a < b) {
                strictlyLess = true;
            }
        }

        return strictlyLess;
    }

    @Override
    public boolean happensAfter(VectorClock other) {
        return other.happensBefore(this);
    }

    @Override
    public boolean isConcurrent(VectorClock other) {
        return !happensBefore(other) && !happensAfter(other);
    }

    @Override
    public long[] getClock() {
        return Arrays.copyOf(vectorClock, vectorClock.length);
    }

    private void ensureCapacity(int size) {
        if (vectorClock.length < size) {
            vectorClock = Arrays.copyOf(vectorClock, size);
        }
    }
}