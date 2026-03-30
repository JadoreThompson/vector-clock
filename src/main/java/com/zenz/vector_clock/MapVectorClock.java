package com.zenz.vector_clock;

import java.util.HashMap;

/**
 * Maintains a hash map to keep track of each node's counter
 */

public class MapVectorClock extends BaseVectorClock<MapVectorClock> {

    private final HashMap<String, Long> counters;
    private final String id;

    public MapVectorClock(String id) {
        this.id = id;
        this.counters = new HashMap<>();
    }

    private MapVectorClock(String id, HashMap<String, Long> counters) {
        this.id = id;
        this.counters = counters;
    }

    @Override
    public void increment() {
        this.counters.put(this.id, this.counters.getOrDefault(this.id, 0L) + 1);
    }

    @Override
    public void merge(MapVectorClock other) {
        for (String key : other.counters.keySet()) {
            this.counters.put(key, Math.max(this.counters.get(key), other.counters.get(key)));
        }
    }

    @Override
    public boolean happensBefore(MapVectorClock other) {
        var clocks = other.counters.size() > this.counters.size() ? other.counters : this.counters;
        boolean strictlyLess = false;

        for (String key : clocks.keySet()) {
            long a = this.counters.get(key);
            long b = clocks.get(key);

            if (a > b) {
                return false;
            }

            if (a < b) {
                strictlyLess = true;
            }
        }

        return strictlyLess;
    }

    @Override
    public boolean happensAfter(MapVectorClock other) {
        return other.happensBefore(this);
    }

    @Override
    public boolean isConcurrent(MapVectorClock other) {
        return !happensBefore(other) && happensAfter(other);
    }

    @Override
    public MapVectorClock duplicate() {
        return new MapVectorClock(this.id, new HashMap<>(this.counters));
    }


    @Override
    public String getId() {
        return id;
    }
}
