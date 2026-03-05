package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.graph.DependencyGraph;
import com.archsentinel.graph.GraphEdge;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LayerViolationDetector {

    public List<String> detect(List<ClassInfo> classes, DependencyGraph graph) {
        List<String> violations = new ArrayList<>();

        Map<String, ClassInfo.ClassType> typeMap = classes.stream()
                .collect(Collectors.toMap(ClassInfo::getClassName, ClassInfo::getType));

        for (GraphEdge edge : graph.getEdges()) {
            ClassInfo.ClassType fromType = typeMap.getOrDefault(edge.getFrom(), ClassInfo.ClassType.UNKNOWN);
            ClassInfo.ClassType toType = typeMap.getOrDefault(edge.getTo(), ClassInfo.ClassType.UNKNOWN);

            // Controller should not directly depend on Repository
            if (fromType == ClassInfo.ClassType.CONTROLLER && toType == ClassInfo.ClassType.REPOSITORY) {
                violations.add(String.format("LAYER_VIOLATION: Controller '%s' directly depends on Repository '%s' (should go through Service)",
                        edge.getFrom(), edge.getTo()));
            }

            // Entity should not depend on Service or Controller
            if (fromType == ClassInfo.ClassType.ENTITY &&
                    (toType == ClassInfo.ClassType.SERVICE || toType == ClassInfo.ClassType.CONTROLLER)) {
                violations.add(String.format("LAYER_VIOLATION: Entity '%s' depends on %s '%s'",
                        edge.getFrom(), toType, edge.getTo()));
            }
        }

        return violations;
    }
}
