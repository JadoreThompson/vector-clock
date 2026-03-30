package com.zenz.vector_clock;

public abstract class BaseVectorClock<T extends BaseVectorClock<T>> {

    public abstract void increment();

    public abstract void merge(T other);

    public abstract boolean happensBefore(T other);

    public abstract boolean happensAfter(T other);

    public abstract boolean isConcurrent(T other);

    public abstract T duplicate();

    public abstract String getId();
}
