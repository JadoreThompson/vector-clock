# Vector Clock

A Java implementation of vector clocks for tracking causality in distributed systems where physical time is unreliable.

## What are Vector Clocks?

Vector clocks capture causal relationships between events in distributed systems. Each node maintains a counter array,
incrementing its own position on each event. When nodes communicate, they merge clocks by taking the maximum of each
position.

**Key advantage over Lamport's Logical Clocks**: Vector clocks can distinguish concurrent events from causally related
ones.

## Installation

Add the dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>com.zenz</groupId>
    <artifactId>vector-clock</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Implementations

| Implementation      | Use Case                           | Node ID Type |
|---------------------|------------------------------------|--------------|
| `SimpleVectorClock` | Fixed node count, known beforehand | `int`        |
| `MapVectorClock`    | Dynamic nodes, production systems  | `String`     |

## Quick Start

### MapVectorClock (Recommended)

```java
// Create clocks for two nodes
MapVectorClock nodeA = new MapVectorClock("node-A");
MapVectorClock nodeB = new MapVectorClock("node-B");

// Increment on local events
nodeA.

increment();

// Merge clocks when receiving messages
nodeB.

merge(nodeA);

// Check causal relationships
nodeA.

happensBefore(nodeB);  // true
nodeB.

happensAfter(nodeA);   // true
nodeA.

isConcurrent(nodeB);   // false
```

### SimpleVectorClock

```java
// Node 0 with 3 total nodes
SimpleVectorClock node0 = new SimpleVectorClock(0);
SimpleVectorClock node1 = new SimpleVectorClock(1);

node0.

increment();
node1.

merge(node0);

node0.

happensBefore(node1);  // true
```

## API Reference

| Method                   | Description                                          |
|--------------------------|------------------------------------------------------|
| `increment()`            | Increment this node's counter                        |
| `merge(T other)`         | Merge with another clock (take max of each position) |
| `happensBefore(T other)` | Returns `true` if this clock causally precedes other |
| `happensAfter(T other)`  | Returns `true` if this clock causally follows other  |
| `isConcurrent(T other)`  | Returns `true` if neither happens before the other   |
| `duplicate()`            | Create an independent copy                           |
| `getClock()`             | Get the underlying clock data                        |
| `getId()`                | Get this node's identifier                           |

## Causal Ordering

The `sort` method in `MapVectorClock` groups clocks by their causal relationships:

```java
List<MapVectorClock> clocks = List.of(clock1, clock2, clock3);
List<List<MapVectorClock>> sorted = MapVectorClock.sort(clocks);
// Returns groups where:
// - Clocks in the same group are concurrent
// - Groups are ordered by happens-before relationships
```

## Time Ordering Rules

Given two vector clocks V and W:

- **V ≤ W**: Every element in V is ≤ corresponding element in W
- **V < W**: V ≤ W and at least one element is strictly smaller (V happens before W)
- **V || W**: Neither V ≤ W nor W ≤ V (concurrent events)

## Building

```bash
.\mvnw clean install
```

## Testing

```bash
.\mvnw test
```