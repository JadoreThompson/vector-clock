package com.zenz.vector_clock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DistributedVectorClockTest {

    // === increment Tests ===

    @Test
    void testIncrement() {
        DistributedVectorClock dvc = new DistributedVectorClock(1);

        long[] vectorClock = dvc.getVectorClock();
        Assertions.assertTrue(vectorClock.length == 2, "Vector clock should be initialised with a length of 2 since id is 1");
        Assertions.assertTrue(vectorClock[dvc.getId()] == 0, "No events have been processed yet so counter must be 0");

        dvc.increment();
        vectorClock = dvc.getVectorClock();
        Assertions.assertTrue(vectorClock[dvc.getId()] == 1, "Vector clock should be incremented");
        Assertions.assertTrue(vectorClock.length == 2, "Vector clock length should be 2");
    }

    // === merge Tests ===

    @Test
    void testMerge() {
        DistributedVectorClock dvc = new DistributedVectorClock(1);

        // Testing increments
        dvc.increment();
        dvc.increment();
        dvc.increment();
        Assertions.assertTrue(dvc.getVectorClock()[dvc.getId()] == 3, "Vector clock should be incremented");

        // Preparing second vector clock
        DistributedVectorClock dvc2 = new DistributedVectorClock(0);
        dvc2.increment();

        // Testing merge
        dvc.merge(dvc2);
        long[] vectorClock = dvc.getVectorClock();

        Assertions.assertTrue(vectorClock.length == 2, "Vector clock length should be 2 since id is 1");
        Assertions.assertTrue(vectorClock[0] == 1, "Counter at position 0 should be 1");
        Assertions.assertTrue(vectorClock[1] == 3, "Counter at position 1 should be 3");
    }

    // === happensBefore Tests ===

    @Test
    void testHappensBefore_SingleElementLess() {
        // V = [1, 0], W = [2, 0] -> V happens before W
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 0}, 0);

        Assertions.assertTrue(v.happensBefore(w), "V=[1,0] should happen before W=[2,0]");
        Assertions.assertFalse(w.happensBefore(v), "W=[2,0] should not happen before V=[1,0]");
    }

    @Test
    void testHappensBefore_AllElementsLessOrEqual() {
        // V = [1, 2], W = [2, 3] -> V happens before W
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 3}, 0);

        Assertions.assertTrue(v.happensBefore(w), "V=[1,2] should happen before W=[2,3]");
    }

    @Test
    void testHappensBefore_EqualClocks() {
        // V = [1, 2], W = [1, 2] -> neither happens before (they are equal)
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 2}, 0);

        Assertions.assertFalse(v.happensBefore(w), "Equal clocks should not have happens-before relationship");
        Assertions.assertFalse(w.happensBefore(v), "Equal clocks should not have happens-before relationship");
    }

    @Test
    void testHappensBefore_ConcurrentClocks() {
        // V = [1, 2], W = [2, 1] -> concurrent (neither happens before)
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 1}, 0);

        Assertions.assertFalse(v.happensBefore(w), "V=[1,2] and W=[2,1] are concurrent");
        Assertions.assertFalse(w.happensBefore(v), "V=[1,2] and W=[2,1] are concurrent");
    }

    @Test
    void testHappensBefore_DifferentLengths() {
        // V = [1], W = [1, 1] -> V happens before W (missing elements treated as 0)
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 1}, 0);

        Assertions.assertTrue(v.happensBefore(w), "V=[1] should happen before W=[1,1]");
        Assertions.assertFalse(w.happensBefore(v), "W=[1,1] should not happen before V=[1]");
    }

    @Test
    void testHappensBefore_DifferentLengthsConcurrent() {
        // V = [2], W = [1, 1] -> concurrent
        DistributedVectorClock v = new DistributedVectorClock(new long[]{2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 1}, 0);

        Assertions.assertFalse(v.happensBefore(w), "V=[2] and W=[1,1] are concurrent");
        Assertions.assertFalse(w.happensBefore(v), "V=[2] and W=[1,1] are concurrent");
    }

    @Test
    void testHappensBefore_ZeroClock() {
        // V = [0, 0], W = [1, 0] -> V is an empty clock
        DistributedVectorClock v = new DistributedVectorClock(new long[]{0, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 0}, 0);

        Assertions.assertThrows(
                VectorClockException.class,
                () -> v.happensBefore(w),
                "Vector v has an empty clock. Exception expected"
        );
    }

    @Test
    void testHappensBefore_SequentialEvents() {
        // Simulating sequential events on same node
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 0}, 0);
        DistributedVectorClock x = new DistributedVectorClock(new long[]{3, 0}, 0);

        Assertions.assertTrue(v.happensBefore(w), "V=[1,0] should happen before W=[2,0]");
        Assertions.assertTrue(w.happensBefore(x), "W=[2,0] should happen before X=[3,0]");
        Assertions.assertTrue(v.happensBefore(x), "Transitivity: V should happen before X");
    }

    // === happensAfter Tests ===

    @Test
    void testHappensAfter_SingleElementGreater() {
        // V = [2, 0], W = [1, 0] -> V happens after W
        DistributedVectorClock v = new DistributedVectorClock(new long[]{2, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 0}, 0);

        Assertions.assertTrue(v.happensAfter(w), "V=[2,0] should happen after W=[1,0]");
        Assertions.assertFalse(w.happensAfter(v), "W=[1,0] should not happen after V=[2,0]");
    }

    @Test
    void testHappensAfter_AllElementsGreaterOrEqual() {
        // V = [2, 3], W = [1, 2] -> V happens after W
        DistributedVectorClock v = new DistributedVectorClock(new long[]{2, 3}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 2}, 0);

        Assertions.assertTrue(v.happensAfter(w), "V=[2,3] should happen after W=[1,2]");
    }

    @Test
    void testHappensAfter_EqualClocks() {
        // V = [1, 2], W = [1, 2] -> neither happens after
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 2}, 0);

        Assertions.assertFalse(v.happensAfter(w), "Equal clocks should not have happens-after relationship");
        Assertions.assertFalse(w.happensAfter(v), "Equal clocks should not have happens-after relationship");
    }

    @Test
    void testHappensAfter_ConcurrentClocks() {
        // V = [1, 2], W = [2, 1] -> concurrent (neither happens after)
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 1}, 0);

        Assertions.assertFalse(v.happensAfter(w), "V=[1,2] and W=[2,1] are concurrent");
        Assertions.assertFalse(w.happensAfter(v), "V=[1,2] and W=[2,1] are concurrent");
    }

    @Test
    void testHappensAfter_DifferentLengths() {
        // V = [1, 1], W = [1] -> V happens after W (missing elements treated as 0)
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 1}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1}, 0);

        Assertions.assertTrue(v.happensAfter(w), "V=[1,1] should happen after W=[1]");
        Assertions.assertFalse(w.happensAfter(v), "W=[1] should not happen after V=[1,1]");
    }

    @Test
    void testHappensAfter_SequentialEvents() {
        // Simulating sequential events on same node
        DistributedVectorClock v = new DistributedVectorClock(new long[]{3, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 0}, 0);
        DistributedVectorClock x = new DistributedVectorClock(new long[]{1, 0}, 0);

        Assertions.assertTrue(v.happensAfter(w), "V=[3,0] should happen after W=[2,0]");
        Assertions.assertTrue(w.happensAfter(x), "W=[2,0] should happen after X=[1,0]");
        Assertions.assertTrue(v.happensAfter(x), "Transitivity: V should happen after X");
    }

    // === isConcurrent Tests ===

    @Test
    void testIsConcurrent_BasicCase() {
        // V = [1, 2], W = [2, 1] -> concurrent
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 1}, 0);

        Assertions.assertTrue(v.isConcurrent(w), "V=[1,2] and W=[2,1] should be concurrent");
        Assertions.assertTrue(w.isConcurrent(v), "Concurrent relationship should be symmetric");
    }

    @Test
    void testIsConcurrent_NotConcurrent_HappensBefore() {
        // V = [1, 0], W = [2, 0] -> V happens before W, not concurrent
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{2, 0}, 0);

        Assertions.assertFalse(v.isConcurrent(w), "V=[1,0] happens before W=[2,0], not concurrent");
        Assertions.assertFalse(w.isConcurrent(v), "W=[2,0] happens after V=[1,0], not concurrent");
    }

    @Test
    void testIsConcurrent_EqualClocks() {
        // V = [1, 2], W = [1, 2] -> equal clocks are not concurrent
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 2}, 0);

        Assertions.assertFalse(v.isConcurrent(w), "Equal clocks should not be concurrent");
        Assertions.assertFalse(w.isConcurrent(v), "Equal clocks should not be concurrent");
    }

    @Test
    void testIsConcurrent_DifferentLengths() {
        // V = [2], W = [1, 1] -> concurrent
        DistributedVectorClock v = new DistributedVectorClock(new long[]{2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 1}, 0);

        Assertions.assertTrue(v.isConcurrent(w), "V=[2] and W=[1,1] should be concurrent");
        Assertions.assertTrue(w.isConcurrent(v), "Concurrent relationship should be symmetric");
    }

    @Test
    void testIsConcurrent_ThreeNodeScenario() {
        // Simulating three nodes with independent events
        // Node 0: [1, 0, 0], Node 1: [0, 1, 0], Node 2: [0, 0, 1]
        // All are concurrent with each other
        DistributedVectorClock node0 = new DistributedVectorClock(new long[]{1, 0, 0}, 0);
        DistributedVectorClock node1 = new DistributedVectorClock(new long[]{0, 1, 0}, 1);
        DistributedVectorClock node2 = new DistributedVectorClock(new long[]{0, 0, 1}, 2);

        Assertions.assertTrue(node0.isConcurrent(node1), "Independent events should be concurrent");
        Assertions.assertTrue(node1.isConcurrent(node0), "Concurrent relationship should be symmetric");
        Assertions.assertTrue(node0.isConcurrent(node2), "Independent events should be concurrent");
        Assertions.assertTrue(node1.isConcurrent(node2), "Independent events should be concurrent");
    }

    @Test
    void testIsConcurrent_AfterMerge() {
        // Node 0 has [1, 0], Node 1 has [0, 1]
        // After Node 0 merges with Node 1, Node 0 has [1, 1]
        // Now Node 0 happens after both original states
        DistributedVectorClock node0Before = new DistributedVectorClock(new long[]{1, 0}, 0);
        DistributedVectorClock node1Before = new DistributedVectorClock(new long[]{0, 1}, 1);

        // Before merge, they are concurrent
        Assertions.assertTrue(node0Before.isConcurrent(node1Before), "Before merge, clocks should be concurrent");

        // After merge
        DistributedVectorClock node0After = new DistributedVectorClock(new long[]{1, 0}, 0);
        node0After.merge(node1Before);

        Assertions.assertFalse(node0After.isConcurrent(node1Before), "After merge, node0 should happen after node1");
        Assertions.assertTrue(node0After.happensAfter(node1Before), "After merge, node0 should happen after node1");
        Assertions.assertTrue(node0After.happensAfter(node0Before), "After merge, node0 should happen after its previous state");
    }

    @Test
    void testIsConcurrent_AllZeroClocks() {
        // V = [0, 0], W = [0, 0] -> equal, not concurrent
        DistributedVectorClock v = new DistributedVectorClock(new long[]{0, 0}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{0, 0}, 0);

        Assertions.assertFalse(v.isConcurrent(w), "Equal all-zero clocks should not be concurrent");
    }

    @Test
    void testIsConcurrent_PartialOverlap() {
        // V = [2, 1], W = [1, 2] -> concurrent (partial overlap)
        DistributedVectorClock v = new DistributedVectorClock(new long[]{2, 1}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 2}, 0);

        Assertions.assertTrue(v.isConcurrent(w), "V=[2,1] and W=[1,2] should be concurrent");
    }

    @Test
    void testIsConcurrent_OneElementDifference() {
        // V = [1, 1, 2], W = [1, 1, 3] -> V happens before W
        DistributedVectorClock v = new DistributedVectorClock(new long[]{1, 1, 2}, 0);
        DistributedVectorClock w = new DistributedVectorClock(new long[]{1, 1, 3}, 0);

        Assertions.assertFalse(v.isConcurrent(w), "V=[1,1,2] happens before W=[1,1,3]");
        Assertions.assertTrue(v.happensBefore(w), "V=[1,1,2] should happen before W=[1,1,3]");
    }
}
