package com.zenz.vector_clock;

public interface BaseVectorClock<T extends BaseVectorClock<T, C>, C> {

    void increment();

    void merge(T other);

    boolean happensBefore(T other);

    boolean happensAfter(T other);

    boolean isConcurrent(T other);

    T duplicate();

    String getId();

    C getClock();
}