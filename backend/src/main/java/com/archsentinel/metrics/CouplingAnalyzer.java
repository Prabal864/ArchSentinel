package com.archsentinel.metrics;

import com.archsentinel.graph.DependencyGraph;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CouplingAnalyzer {

    public double analyze(DependencyGraph graph) {
        if (graph.getNodeNames().isEmpty()) {
            return 0.0;
        }

        Map<String, java.util.List<String>> adj = graph.getAdjacencyList();

        double totalCoupling = 0;
        for (String node : graph.getNodeNames()) {
            int fanOut = adj.getOrDefault(node, java.util.Collections.emptyList()).size();
            long fanIn = graph.getEdges().stream()
                    .filter(e -> e.getTo().equals(node))
                    .count();
            totalCoupling += fanIn + fanOut;
        }

        double avgCoupling = totalCoupling / graph.getNodeNames().size();
        // Normalize: assume max reasonable coupling is 20
        return Math.min(100.0, (avgCoupling / 20.0) * 100.0);
    }

    /**
     * Returns per-class coupling details sorted by total coupling (highest first).
     * Each map contains: className, fanIn, fanOut, totalCoupling, type
     */
    public List<Map<String, Object>> analyzePerClass(DependencyGraph graph) {
        List<Map<String, Object>> hotspots = new ArrayList<>();
        if (graph.getNodeNames().isEmpty()) {
            return hotspots;
        }

        Map<String, List<String>> adj = graph.getAdjacencyList();

        for (String node : graph.getNodeNames()) {
            int fanOut = adj.getOrDefault(node, Collections.emptyList()).size();
            long fanIn = graph.getEdges().stream()
                    .filter(e -> e.getTo().equals(node))
                    .count();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("className", node);
            entry.put("type", graph.getNodes().containsKey(node) ? graph.getNodes().get(node).getType() : "UNKNOWN");
            entry.put("fanIn", (int) fanIn);
            entry.put("fanOut", fanOut);
            entry.put("totalCoupling", (int) fanIn + fanOut);

            // Determine dependencies
            List<String> dependsOn = adj.getOrDefault(node, Collections.emptyList());
            entry.put("dependsOn", dependsOn);

            List<String> dependedBy = graph.getEdges().stream()
                    .filter(e -> e.getTo().equals(node))
                    .map(e -> e.getFrom())
                    .collect(Collectors.toList());
            entry.put("dependedBy", dependedBy);

            hotspots.add(entry);
        }

        // Sort by total coupling descending
        hotspots.sort((a, b) -> Integer.compare(
                (int) b.get("totalCoupling"), (int) a.get("totalCoupling")));

        return hotspots;
    }
}
