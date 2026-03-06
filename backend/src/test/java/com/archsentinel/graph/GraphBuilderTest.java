package com.archsentinel.graph;

import com.archsentinel.analyzer.ClassInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphBuilderTest {

    private GraphBuilder graphBuilder;

    @BeforeEach
    void setUp() {
        graphBuilder = new GraphBuilder();
    }

    @Test
    void shouldBuildEmptyGraphFromEmptyList() {
        DependencyGraph graph = graphBuilder.build(List.of());
        assertTrue(graph.getNodeNames().isEmpty());
        assertTrue(graph.getEdges().isEmpty());
    }

    @Test
    void shouldCreateNodesForEachClass() {
        ClassInfo service = new ClassInfo();
        service.setClassName("OrderService");
        service.setType(ClassInfo.ClassType.SERVICE);

        ClassInfo repo = new ClassInfo();
        repo.setClassName("OrderRepository");
        repo.setType(ClassInfo.ClassType.REPOSITORY);

        DependencyGraph graph = graphBuilder.build(List.of(service, repo));

        assertEquals(2, graph.getNodeNames().size());
        assertTrue(graph.getNodeNames().contains("OrderService"));
        assertTrue(graph.getNodeNames().contains("OrderRepository"));
    }

    @Test
    void shouldCreateEdgesForDependencies() {
        ClassInfo service = new ClassInfo();
        service.setClassName("OrderService");
        service.setType(ClassInfo.ClassType.SERVICE);
        service.setDependencies(List.of("OrderRepository"));

        ClassInfo repo = new ClassInfo();
        repo.setClassName("OrderRepository");
        repo.setType(ClassInfo.ClassType.REPOSITORY);

        DependencyGraph graph = graphBuilder.build(List.of(service, repo));

        assertEquals(1, graph.getEdges().size());
        GraphEdge edge = graph.getEdges().get(0);
        assertEquals("OrderService", edge.getFrom());
        assertEquals("OrderRepository", edge.getTo());
        assertEquals("DEPENDENCY", edge.getType());
    }

    @Test
    void shouldNotCreateEdgesForUnknownDependencies() {
        ClassInfo service = new ClassInfo();
        service.setClassName("OrderService");
        service.setType(ClassInfo.ClassType.SERVICE);
        service.setDependencies(List.of("UnknownClass"));

        DependencyGraph graph = graphBuilder.build(List.of(service));

        assertTrue(graph.getEdges().isEmpty(), "Should not add edges for unknown dependencies");
    }

    @Test
    void shouldSerializeGraphToMap() {
        ClassInfo service = new ClassInfo();
        service.setClassName("TestService");
        service.setType(ClassInfo.ClassType.SERVICE);

        DependencyGraph graph = graphBuilder.build(List.of(service));
        java.util.Map<String, Object> map = graph.toMap();

        assertNotNull(map.get("nodes"));
        assertNotNull(map.get("edges"));
    }
}
