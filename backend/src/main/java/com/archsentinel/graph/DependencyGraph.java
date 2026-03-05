package com.archsentinel.graph;

import java.util.*;

public class DependencyGraph {

    private final Map<String, GraphNode> nodes;
    private final List<GraphEdge> edges;
    private final Map<String, List<String>> adjacencyList;

    public DependencyGraph() {
        this.nodes = new LinkedHashMap<>();
        this.edges = new ArrayList<>();
        this.adjacencyList = new LinkedHashMap<>();
    }

    public void addNode(GraphNode node) {
        nodes.put(node.getName(), node);
        adjacencyList.putIfAbsent(node.getName(), new ArrayList<>());
    }

    public void addEdge(GraphEdge edge) {
        edges.add(edge);
        adjacencyList.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>())
                .add(edge.getTo());
        adjacencyList.putIfAbsent(edge.getTo(), new ArrayList<>());
    }

    public Map<String, GraphNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public List<GraphEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public Map<String, List<String>> getAdjacencyList() {
        return Collections.unmodifiableMap(adjacencyList);
    }

    public Set<String> getNodeNames() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    public List<String> getNeighbors(String nodeName) {
        return adjacencyList.getOrDefault(nodeName, Collections.emptyList());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> nodeList = new ArrayList<>();
        nodes.forEach((name, node) -> {
            Map<String, String> n = new LinkedHashMap<>();
            n.put("name", node.getName());
            n.put("type", node.getType());
            nodeList.add(n);
        });
        result.put("nodes", nodeList);

        List<Map<String, String>> edgeList = new ArrayList<>();
        edges.forEach(edge -> {
            Map<String, String> e = new LinkedHashMap<>();
            e.put("from", edge.getFrom());
            e.put("to", edge.getTo());
            e.put("type", edge.getType());
            edgeList.add(e);
        });
        result.put("edges", edgeList);
        return result;
    }
}
