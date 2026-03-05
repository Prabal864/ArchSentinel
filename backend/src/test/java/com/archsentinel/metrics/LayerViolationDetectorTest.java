package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.graph.DependencyGraph;
import com.archsentinel.graph.GraphEdge;
import com.archsentinel.graph.GraphNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LayerViolationDetectorTest {

    private LayerViolationDetector detector;

    @BeforeEach
    void setUp() {
        detector = new LayerViolationDetector();
    }

    @Test
    void shouldDetectControllerToRepositoryViolation() {
        ClassInfo controller = new ClassInfo();
        controller.setClassName("OrderController");
        controller.setType(ClassInfo.ClassType.CONTROLLER);

        ClassInfo repo = new ClassInfo();
        repo.setClassName("OrderRepository");
        repo.setType(ClassInfo.ClassType.REPOSITORY);

        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("OrderController", "CONTROLLER"));
        graph.addNode(new GraphNode("OrderRepository", "REPOSITORY"));
        graph.addEdge(new GraphEdge("OrderController", "OrderRepository", "DEPENDENCY"));

        List<String> violations = detector.detect(List.of(controller, repo), graph);
        assertFalse(violations.isEmpty(), "Should detect Controller→Repository violation");
        assertTrue(violations.get(0).contains("LAYER_VIOLATION"));
    }

    @Test
    void shouldDetectEntityToServiceViolation() {
        ClassInfo entity = new ClassInfo();
        entity.setClassName("Order");
        entity.setType(ClassInfo.ClassType.ENTITY);

        ClassInfo service = new ClassInfo();
        service.setClassName("OrderService");
        service.setType(ClassInfo.ClassType.SERVICE);

        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("Order", "ENTITY"));
        graph.addNode(new GraphNode("OrderService", "SERVICE"));
        graph.addEdge(new GraphEdge("Order", "OrderService", "DEPENDENCY"));

        List<String> violations = detector.detect(List.of(entity, service), graph);
        assertFalse(violations.isEmpty(), "Should detect Entity→Service violation");
    }

    @Test
    void shouldNotDetectViolationForValidDependencies() {
        ClassInfo controller = new ClassInfo();
        controller.setClassName("OrderController");
        controller.setType(ClassInfo.ClassType.CONTROLLER);

        ClassInfo service = new ClassInfo();
        service.setClassName("OrderService");
        service.setType(ClassInfo.ClassType.SERVICE);

        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new GraphNode("OrderController", "CONTROLLER"));
        graph.addNode(new GraphNode("OrderService", "SERVICE"));
        graph.addEdge(new GraphEdge("OrderController", "OrderService", "DEPENDENCY"));

        List<String> violations = detector.detect(List.of(controller, service), graph);
        assertTrue(violations.isEmpty(), "Controller→Service should be valid");
    }

    @Test
    void shouldReturnEmptyForEmptyGraph() {
        List<String> violations = detector.detect(List.of(), new DependencyGraph());
        assertTrue(violations.isEmpty());
    }
}
