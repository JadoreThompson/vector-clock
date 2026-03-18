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