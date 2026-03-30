package com.zenz.vector_clock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapVectorClockTest {

    // === Duplicate Tests ===

    @Test
    void testDuplicate_EmptyClock() {
        MapVectorClock original = new MapVectorClock("node-A");
        MapVectorClock copy = original.duplicate();

        Assertions.assertEquals(original.getId(), copy.getId(), "Duplicate should have same node ID");
        Assertions.assertFalse(original.happensBefore(copy), "Empty clocks should be equal");
        Assertions.assertFalse(copy.happensBefore(original), "Empty clocks should be equal");
    }

    @Test
    void testDuplicate_AfterIncrement() {
        MapVectorClock original = new MapVectorClock("node-A");
        original.increment();
        original.increment();
        original.increment();

        MapVectorClock copy = original.duplicate();

        Assertions.assertEquals(original.getId(), copy.getId());
        Assertions.assertFalse(copy.happensBefore(original), "Copy should not happen before original (they are equal)");
        Assertions.assertFalse(original.happensBefore(copy), "Original should not happen before copy (they are equal)");
    }

    @Test
    void testDuplicate_IsIndependent_OriginalModification() {
        MapVectorClock original = new MapVectorClock("node-A");
        original.increment();

        MapVectorClock copy = original.duplicate();

        original.increment();
        original.increment();

        Assertions.assertTrue(original.happensAfter(copy), 
            "After modifying original, original should happen after copy");
        Assertions.assertFalse(copy.happensAfter(original), 
            "Copy should not happen after original");
    }

    @Test
    void testDuplicate_IsIndependent_CopyModification() {
        MapVectorClock original = new MapVectorClock("node-A");
        original.increment();

        MapVectorClock copy = original.duplicate();

        copy.increment();
        copy.increment();

        Assertions.assertTrue(copy.happensAfter(original), 
            "After modifying copy, copy should happen after original");
        Assertions.assertFalse(original.happensAfter(copy), 
            "Original should not happen after copy");
    }

    @Test
    void testDuplicate_WithMergedData() {
        MapVectorClock original = new MapVectorClock("node-A");
        original.increment();
        original.increment();

        MapVectorClock other = new MapVectorClock("node-B");
        other.increment();

        original.merge(other);

        MapVectorClock copy = original.duplicate();

        Assertions.assertFalse(copy.happensBefore(original), 
            "Copy should not happen before original (they are equal after merge)");
        Assertions.assertFalse(original.happensBefore(copy), 
            "Original should not happen before copy (they are equal after merge)");
    }

    @Test
    void testDuplicate_MultipleNodes() {
        MapVectorClock original = new MapVectorClock("node-A");
        original.increment();
        original.increment();

        MapVectorClock nodeB = new MapVectorClock("node-B");
        nodeB.increment();
        nodeB.increment();
        nodeB.increment();

        original.merge(nodeB);

        MapVectorClock copy = original.duplicate();

        Assertions.assertFalse(copy.happensBefore(original), 
            "Copy should not happen before original (they are equal)");
        Assertions.assertFalse(original.happensBefore(copy), 
            "Original should not happen before copy (they are equal)");
    }

    @Test
    void testDuplicate_PreservesConcurrentRelationship() {
        MapVectorClock v = new MapVectorClock("node-A");
        v.increment();

        MapVectorClock w = new MapVectorClock("node-B");
        w.increment();

        Assertions.assertTrue(v.isConcurrent(w), "node-A and node-B should be concurrent");

        MapVectorClock vCopy = v.duplicate();

        Assertions.assertFalse(v.happensBefore(w), "node-A and node-B are concurrent");
        Assertions.assertFalse(w.happensBefore(v), "node-B and node-A are concurrent");
    }
}
