package com.zenz.vector_clock;

import java.util.Arrays;

//public final class SimpleVectorClock implements VectorClock {
public final class SimpleVectorClock {
    private long[] vectorClock;
    private final int id;

    public SimpleVectorClock(int id) {
        this.id = id;
        this.vectorClock = new long[id + 1];
    }

    public SimpleVectorClock(long[] vectorClock, int id) {
        if (id < 0 || id >= vectorClock.length) {
            throw new IllegalArgumentException("Invalid node id");
        }

        this.vectorClock = Arrays.copyOf(vectorClock, vectorClock.length);
        this.id = id;
    }

    public void increment() {
        vectorClock[id]++;
    }

    public void merge(SimpleVectorClock other) {
        long[] otherClock = other.getVectorClock();
        ensureCapacity(otherClock.length);

        for (int i = 0; i < otherClock.length; i++) {
            vectorClock[i] = Math.max(vectorClock[i], otherClock[i]);
        }
    }

    /**
     * Determines whether this vector clock happens before another vector clock.
     *
     * <p><b>Time Ordering Rules:</b></p>
     * <ul>
     *   <li><b>V ≤ W</b> if every element in V is less than or equal to W</li>
     *   <li><b>V &lt; W</b> if V ≤ W and at least one element is strictly smaller</li>
     *   <li><b>V &gt; W</b> if W &lt; V</li>
     *   <li>If neither V ≤ W nor W ≤ V, the events are <b>concurrent</b></li>
     * </ul>
     *
     * <p>This method returns {@code true} if this vector clock (V) happens before
     * the given vector clock (W), i.e.:</p>
     *
     * <ul>
     *   <li>For all indices i: V[i] ≤ W[i]</li>
     *   <li>There exists at least one index j such that V[j] &lt; W[j]</li>
     * </ul>
     *
     * <p>If these conditions are not satisfied, then this vector clock does not
     * happen before the other (it may either happen after or be concurrent).</p>
     *
     * @param other the vector clock to compare against
     * @return {@code true} if this clock happens before {@code other}, otherwise {@code false}
     */
    public boolean happensBefore(SimpleVectorClock other) {
        if (Arrays.stream(vectorClock).allMatch(v -> v == 0L)) {
            throw new VectorClockException("Node " + id + " has empty vector clock");
        }

        if (Arrays.stream(other.getVectorClock()).allMatch(v -> v == 0L)) {
            throw new VectorClockException("Node " + other.getId() + " has empty vector clock");
        }

        long[] otherClock = other.getVectorClock();
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

    public boolean happensAfter(SimpleVectorClock other) {
        return other.happensBefore(this);
    }

    public boolean isConcurrent(SimpleVectorClock other) {
        if (Arrays.equals(vectorClock, other.getVectorClock())) return false;
        return !happensBefore(other) && !happensAfter(other);
    }

    public long[] getVectorClock() {
        return Arrays.copyOf(vectorClock, vectorClock.length);
    }

    void ensureCapacity(int size) {
        if (vectorClock.length < size) {
            vectorClock = Arrays.copyOf(vectorClock, size);
        }
    }

    public int getId() {
        return id;
    }

    public SimpleVectorClock duplicate() {
        SimpleVectorClock newVectorClock = new SimpleVectorClock(this.id);
        newVectorClock.vectorClock = Arrays.copyOf(vectorClock, vectorClock.length);
        return newVectorClock;
    }

    public String toString() {
        return "SimpleVectorClock{" +
                "vectorClock=" + Arrays.toString(vectorClock) +
                ", id=" + id +
                '}';
    }
}