package com.archsentinel.metrics;

import com.archsentinel.graph.DependencyGraph;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CircularDependencyDetector {

    private enum Color { WHITE, GRAY, BLACK }

    public List<List<String>> detect(DependencyGraph graph) {
        Map<String, Color> colors = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        List<List<String>> cycles = new ArrayList<>();

        for (String node : graph.getNodeNames()) {
            colors.put(node, Color.WHITE);
        }

        for (String node : graph.getNodeNames()) {
            if (colors.get(node) == Color.WHITE) {
                dfs(node, colors, parent, cycles, graph, new ArrayDeque<>());
            }
        }

        return cycles;
    }

    private void dfs(String node, Map<String, Color> colors, Map<String, String> parent,
                     List<List<String>> cycles, DependencyGraph graph, Deque<String> stack) {
        colors.put(node, Color.GRAY);
        stack.push(node);

        for (String neighbor : graph.getNeighbors(node)) {
            if (colors.getOrDefault(neighbor, Color.WHITE) == Color.GRAY) {
                // Found a cycle - extract it from stack
                List<String> cycle = new ArrayList<>();
                Iterator<String> it = stack.iterator();
                while (it.hasNext()) {
                    String n = it.next();
                    cycle.add(0, n);
                    if (n.equals(neighbor)) break;
                }
                cycle.add(neighbor);
                if (!cycleAlreadyFound(cycles, cycle)) {
                    cycles.add(cycle);
                }
            } else if (colors.getOrDefault(neighbor, Color.WHITE) == Color.WHITE) {
                dfs(neighbor, colors, parent, cycles, graph, stack);
            }
        }

        stack.pop();
        colors.put(node, Color.BLACK);
    }

    private boolean cycleAlreadyFound(List<List<String>> cycles, List<String> newCycle) {
        Set<String> newSet = new HashSet<>(newCycle);
        for (List<String> existing : cycles) {
            if (new HashSet<>(existing).equals(newSet)) {
                return true;
            }
        }
        return false;
    }
}
