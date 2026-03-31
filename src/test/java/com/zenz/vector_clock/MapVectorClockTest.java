package com.zenz.vector_clock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    // === Sort Tests ===

    @Test
    void testSort_EmptyList() {
        List<MapVectorClock> clocks = new ArrayList<>();

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        Assertions.assertTrue(result.isEmpty(), "Sorting an empty list should return empty result");
    }

    @Test
    void testSort_SingleElement() {
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();
        clock1.increment();

        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock1);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        Assertions.assertEquals(1, result.size(), "Result should have one group");
        Assertions.assertEquals(1, result.get(0).size(), "First group should have one element");
        Assertions.assertEquals(clock1, result.get(0).get(0), "First element should be clock1");
    }

    @Test
    void testSort_TwoSequentialClocks_ReturnsCorrectOrder() {
        // Create clock1: node-A has counter 1
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();

        // Create clock2: node-A has counter 2 (happens after clock1)
        MapVectorClock clock2 = new MapVectorClock("node-A");
        clock2.increment();
        clock2.increment();

        // Add in reverse order to test sorting
        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock2);
        clocks.add(clock1);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        // Should have 2 groups since clock1 happens before clock2
        Assertions.assertEquals(2, result.size(), "Result should have two groups for sequential clocks");

        // First group should contain clock1 (earlier clock)
        Assertions.assertEquals(1, result.get(0).size(), "First group should have one element");
        Assertions.assertEquals(clock1, result.get(0).get(0), "First group should contain clock1 (earlier clock)");

        // Second group should contain clock2 (later clock)
        Assertions.assertEquals(1, result.get(1).size(), "Second group should have one element");
        Assertions.assertEquals(clock2, result.get(1).get(0), "Second group should contain clock2 (later clock)");
    }

    @Test
    void testSort_ConcurrentClocks_ReturnsSameGroup() {

        // Create two concurrent clocks from different nodes
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();

        MapVectorClock clock2 = new MapVectorClock("node-B");
        clock2.increment();

        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock1);
        clocks.add(clock2);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        // Concurrent clocks should be in the same group
        Assertions.assertEquals(1, result.size(), "Concurrent clocks should be in one group");
        Assertions.assertEquals(2, result.get(0).size(), "Group should contain both concurrent clocks");

        // Verify both clocks are in the group
        Assertions.assertTrue(result.get(0).contains(clock1), "Group should contain clock1");
        Assertions.assertTrue(result.get(0).contains(clock2), "Group should contain clock2");
    }

    @Test
    void testSort_ThreeSequentialClocks_ReturnsCorrectOrder() {
        MapVectorClock sorter = new MapVectorClock("node-A");

        // Create a causal chain: clock1 -> clock2 -> clock3
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();

        MapVectorClock clock2 = new MapVectorClock("node-A");
        clock2.increment();
        clock2.increment();

        MapVectorClock clock3 = new MapVectorClock("node-A");
        clock3.increment();
        clock3.increment();
        clock3.increment();

        // Add in random order
        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock3);
        clocks.add(clock1);
        clocks.add(clock2);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        // Should have 3 groups for the causal chain
        Assertions.assertEquals(3, result.size(), "Result should have three groups for causal chain");

        // Verify order: clock1 (earliest) -> clock2 -> clock3 (latest)
        Assertions.assertEquals(clock1, result.get(0).get(0), "First group should contain clock1 (earliest)");
        Assertions.assertEquals(clock2, result.get(1).get(0), "Second group should contain clock2");
        Assertions.assertEquals(clock3, result.get(2).get(0), "Third group should contain clock3 (latest)");
    }

    @Test
    void testSort_MixedConcurrentAndSequential_ReturnsCorrectGroups() {

        // clock1: earliest (node-A counter 1)
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();

        // clock2 and clock3: concurrent with each other, both happen after clock1
        MapVectorClock clock2 = new MapVectorClock("node-B");
        clock2.increment();
        clock2.merge(clock1); // clock2 has seen clock1

        MapVectorClock clock3 = new MapVectorClock("node-C");
        clock3.increment();
        clock3.merge(clock1); // clock3 has seen clock1

        // clock4: happens after clock2
        MapVectorClock clock4 = new MapVectorClock("node-A");
        clock4.increment();
        clock4.increment();
        clock4.merge(clock2);

        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock4);
        clocks.add(clock2);
        clocks.add(clock1);
        clocks.add(clock3);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        // First group should contain clock1 (earliest)
        Assertions.assertEquals(clock1, result.get(0).get(0), "First group should contain clock1");

        // Find the group containing clock2 and clock3 (should be same group as they are concurrent)
        // They both happen after clock1 but are concurrent with each other
        int groupWithClock2 = -1;
        int groupWithClock3 = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains(clock2)) groupWithClock2 = i;
            if (result.get(i).contains(clock3)) groupWithClock3 = i;
        }
        Assertions.assertEquals(groupWithClock2, groupWithClock3, "clock2 and clock3 should be in the same group (concurrent)");
        Assertions.assertTrue(groupWithClock2 > 0, "clock2/clock3 group should come after clock1 group");

        // clock4 should be in a later group than clock2
        int groupWithClock4 = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains(clock4)) groupWithClock4 = i;
        }
        Assertions.assertTrue(groupWithClock4 > groupWithClock2, "clock4 should come after clock2");
    }

    @Test
    void testSort_MergedClocks_ReturnsCorrectOrder() {
        // Create clock1 from node-A
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();

        // Create clock2 from node-B, then merge with clock1
        MapVectorClock clock2 = new MapVectorClock("node-B");
        clock2.increment();
        clock2.merge(clock1);

        // Add in reverse order
        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock2);
        clocks.add(clock1);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);
        // Should have 2 groups since clock1 happens before clock2
        Assertions.assertEquals(2, result.size(), "Result should have two groups");

        // clock1 should be in the first group (earlier)
        Assertions.assertEquals(clock1, result.get(0).get(0), "First group should contain clock1 (earlier)");

        // clock2 should be in the second group (later)
        Assertions.assertEquals(clock2, result.get(1).get(0), "Second group should contain clock2 (later)");
    }

    @Test
    void testSort_AllConcurrent_ReturnsSingleGroup() {
        // Create multiple concurrent clocks from different nodes
        MapVectorClock clock1 = new MapVectorClock("node-A");
        clock1.increment();

        MapVectorClock clock2 = new MapVectorClock("node-B");
        clock2.increment();

        MapVectorClock clock3 = new MapVectorClock("node-C");
        clock3.increment();

        MapVectorClock clock4 = new MapVectorClock("node-D");
        clock4.increment();

        List<MapVectorClock> clocks = new ArrayList<>();
        clocks.add(clock3);
        clocks.add(clock1);
        clocks.add(clock4);
        clocks.add(clock2);

        List<List<MapVectorClock>> result = MapVectorClock.sort(clocks);

        // All concurrent clocks should be in a single group
        Assertions.assertEquals(1, result.size(), "All concurrent clocks should be in one group");
        Assertions.assertEquals(4, result.get(0).size(), "Group should contain all four clocks");

        // Verify all clocks are present
        Assertions.assertTrue(result.get(0).contains(clock1), "Group should contain clock1");
        Assertions.assertTrue(result.get(0).contains(clock2), "Group should contain clock2");
        Assertions.assertTrue(result.get(0).contains(clock3), "Group should contain clock3");
        Assertions.assertTrue(result.get(0).contains(clock4), "Group should contain clock4");
    }
}