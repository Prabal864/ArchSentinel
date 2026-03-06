package com.archsentinel.graph;

import com.archsentinel.analyzer.ClassInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GraphBuilder {

    public DependencyGraph build(List<ClassInfo> classes) {
        DependencyGraph graph = new DependencyGraph();

        // Add all nodes
        for (ClassInfo cls : classes) {
            GraphNode node = new GraphNode(cls.getClassName(), cls.getType().name());
            graph.addNode(node);
        }

        Set<String> knownClasses = classes.stream()
                .map(ClassInfo::getClassName)
                .collect(Collectors.toSet());

        // Add edges from dependencies
        for (ClassInfo cls : classes) {
            for (String dep : cls.getDependencies()) {
                String depSimpleName = extractSimpleName(dep);
                if (knownClasses.contains(depSimpleName)) {
                    graph.addEdge(new GraphEdge(cls.getClassName(), depSimpleName, "DEPENDENCY"));
                }
            }
        }

        return graph;
    }

    private String extractSimpleName(String typeName) {
        // Handle generic types like List<OrderService>
        if (typeName.contains("<")) {
            typeName = typeName.substring(typeName.indexOf('<') + 1, typeName.lastIndexOf('>'));
        }
        // Get simple class name from FQN
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot >= 0) {
            return typeName.substring(lastDot + 1);
        }
        return typeName;
    }
}
