package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.graph.DependencyGraph;
import com.archsentinel.graph.GraphBuilder;
import com.archsentinel.graph.GraphEdge;
import com.archsentinel.graph.GraphNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CouplingAnalyzerTest {

    private CouplingAnalyzer couplingAnalyzer;

    @BeforeEach
    void setUp() {
        couplingAnalyzer = new CouplingAnalyzer();
    }

    @Test
    void shouldReturnZeroForEmptyGraph() {
        DependencyGraph graph = new DependencyGraph();
        assertEquals(0.0, couplingAnalyzer.analyze(graph));
    }

    @Test
    void shouldCalculateCouplingForSimpleGraph() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("OrderService", "SERVICE"));
        graph.addNode(new GraphNode("OrderRepository", "REPOSITORY"));
        graph.addNode(new GraphNode("PaymentService", "SERVICE"));

        graph.addEdge(new GraphEdge("OrderService", "OrderRepository", "DEPENDENCY"));
        graph.addEdge(new GraphEdge("OrderService", "PaymentService", "DEPENDENCY"));

        double score = couplingAnalyzer.analyze(graph);
        assertTrue(score >= 0 && score <= 100, "Coupling score should be between 0 and 100");
        assertTrue(score > 0, "Coupling score should be positive");
    }

    @Test
    void shouldReturnHighCouplingForHighlyConnectedGraph() {
        DependencyGraph graph = new DependencyGraph();
        for (int i = 0; i < 5; i++) {
            graph.addNode(new GraphNode("Class" + i, "SERVICE"));
        }
        // Connect all classes to class 0 (high fan-in)
        for (int i = 1; i < 5; i++) {
            graph.addEdge(new GraphEdge("Class" + i, "Class0", "DEPENDENCY"));
        }

        double score = couplingAnalyzer.analyze(graph);
        assertTrue(score > 0, "Score should be positive for connected graph");
    }

    @Test
    void shouldNormalizeScoreTo100() {
        // Create a very highly coupled single-class graph (self-referencing many times via edges)
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("HubClass", "SERVICE"));
        graph.addNode(new GraphNode("OnlyClient", "SERVICE"));
        // Add enough edges so HubClass alone has coupling > 20
        for (int i = 0; i < 15; i++) {
            graph.addEdge(new GraphEdge("HubClass", "OnlyClient", "DEPENDENCY"));
            graph.addEdge(new GraphEdge("OnlyClient", "HubClass", "DEPENDENCY"));
        }

        double score = couplingAnalyzer.analyze(graph);
        assertEquals(100.0, score, "Over-threshold coupling should be capped at 100");
    }
}
