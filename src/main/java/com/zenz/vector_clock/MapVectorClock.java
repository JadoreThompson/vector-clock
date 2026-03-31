package com.zenz.vector_clock;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Maintains a hash map to keep track of each node's counter
 */
public final class MapVectorClock implements BaseVectorClock<MapVectorClock, HashMap<String, Long>> {

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
            this.counters.put(
                    key,
                    Math.max(
                            this.counters.getOrDefault(key, 0L),
                            other.counters.getOrDefault(key, 0L)
                    )
            );
        }
    }

    @Override
    public boolean happensBefore(MapVectorClock other) {
        var clocks = other.counters.size() > this.counters.size() ? other.counters : this.counters;
        boolean strictlyLess = false;

        for (String key : clocks.keySet()) {
            long a = this.counters.getOrDefault(key, 0L);
            long b = other.counters.getOrDefault(key, 0L);

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
        return !happensBefore(other) && !happensAfter(other);
    }

    @Override
    public MapVectorClock duplicate() {
        return new MapVectorClock(this.id, new HashMap<>(this.counters));
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public HashMap<String, Long> getClock() {
        return new HashMap<>(this.counters);
    }

    public static List<List<MapVectorClock>> sort(List<MapVectorClock> clocks) {
        List<List<MapVectorClock>> sortedClocks = new ArrayList<>();

        for (MapVectorClock clock : clocks) {
            if (sortedClocks.isEmpty()) {
                sortedClocks.add(new ArrayList<>());
                sortedClocks.getFirst().add(clock);
                continue;
            }

            AbstractMap.SimpleEntry<Integer, Boolean> result = findIndex(clock, sortedClocks, 0, sortedClocks.size());
            List<MapVectorClock> clockList;
            if (!result.getValue()) {
                clockList = sortedClocks.get(result.getKey());
            } else {
                clockList = new ArrayList<>();
                sortedClocks.add(result.getKey(), clockList);
            }

            clockList.add(clock);
        }

        return sortedClocks;
    }

    private static AbstractMap.SimpleEntry<Integer, Boolean> findIndex(MapVectorClock clock, List<List<MapVectorClock>> clocks, final int left, final int right) {
        final int mp = (left + right) / 2;
        final MapVectorClock mpClock = clocks.get(mp).getFirst();

        if (mpClock.isConcurrent(clock)) {
            return new AbstractMap.SimpleEntry<>(mp, false);
        }

        if (mpClock.happensBefore(clock)) {
            if (left == right) {
                return new AbstractMap.SimpleEntry<>(mp + 1, true);
            }
            return findIndex(clock, clocks, mp + 1, right);
        }

        if (left == right) {
            return new AbstractMap.SimpleEntry<>(mp, true);
        }
        return findIndex(clock, clocks, left, mp);
    }

    @Override
    public String toString() {
        return "MapVectorClock{" +
                "counters=" + counters +
                ", id='" + id + '\'' +
                '}';
    }
}
