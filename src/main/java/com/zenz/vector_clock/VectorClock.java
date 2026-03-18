package com.zenz.vector_clock;

public interface VectorClock {
    void merge(VectorClock other);

    long[] getClock();

    boolean happensBefore(VectorClock other);

    boolean happensAfter(VectorClock other);

    boolean isConcurrent(VectorClock other);
}