package com.zenz.vector_clock;

public interface VectorClock {
    void merge(VectorClock other);

    VectorClock get();

    void happensBefore(VectorClock other);

    void happensAfter(VectorClock other);

    void isConcurrent(VectorClock other);
}