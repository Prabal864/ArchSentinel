package com.archsentinel.metrics;

import com.archsentinel.graph.DependencyGraph;
import com.archsentinel.graph.GraphEdge;
import com.archsentinel.graph.GraphNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CircularDependencyDetectorTest {

    private CircularDependencyDetector detector;

    @BeforeEach
    void setUp() {
        detector = new CircularDependencyDetector();
    }

    @Test
    void shouldDetectNoCyclesInAcyclicGraph() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("A", "SERVICE"));
        graph.addNode(new GraphNode("B", "SERVICE"));
        graph.addNode(new GraphNode("C", "SERVICE"));
        graph.addEdge(new GraphEdge("A", "B", "DEPENDENCY"));
        graph.addEdge(new GraphEdge("B", "C", "DEPENDENCY"));

        List<List<String>> cycles = detector.detect(graph);
        assertTrue(cycles.isEmpty(), "Acyclic graph should have no cycles");
    }

    @Test
    void shouldDetectSimpleCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("A", "SERVICE"));
        graph.addNode(new GraphNode("B", "SERVICE"));
        graph.addEdge(new GraphEdge("A", "B", "DEPENDENCY"));
        graph.addEdge(new GraphEdge("B", "A", "DEPENDENCY"));

        List<List<String>> cycles = detector.detect(graph);
        assertFalse(cycles.isEmpty(), "Should detect cycle A→B→A");
    }

    @Test
    void shouldDetectThreeNodeCycle() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("ServiceA", "SERVICE"));
        graph.addNode(new GraphNode("ServiceB", "SERVICE"));
        graph.addNode(new GraphNode("ServiceC", "SERVICE"));
        graph.addEdge(new GraphEdge("ServiceA", "ServiceB", "DEPENDENCY"));
        graph.addEdge(new GraphEdge("ServiceB", "ServiceC", "DEPENDENCY"));
        graph.addEdge(new GraphEdge("ServiceC", "ServiceA", "DEPENDENCY"));

        List<List<String>> cycles = detector.detect(graph);
        assertFalse(cycles.isEmpty(), "Should detect 3-node cycle");
    }

    @Test
    void shouldHandleEmptyGraph() {
        DependencyGraph graph = new DependencyGraph();
        List<List<String>> cycles = detector.detect(graph);
        assertTrue(cycles.isEmpty());
    }

    @Test
    void shouldHandleSingleNode() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("Alone", "SERVICE"));
        List<List<String>> cycles = detector.detect(graph);
        assertTrue(cycles.isEmpty());
    }
}
