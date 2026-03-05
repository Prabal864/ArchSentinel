package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.graph.DependencyGraph;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsEngine {

    private final CouplingAnalyzer couplingAnalyzer;
    private final CircularDependencyDetector circularDependencyDetector;
    private final ComplexityAnalyzer complexityAnalyzer;
    private final LayerViolationDetector layerViolationDetector;
    private final PerformanceRiskDetector performanceRiskDetector;

    public MetricsEngine(CouplingAnalyzer couplingAnalyzer,
                         CircularDependencyDetector circularDependencyDetector,
                         ComplexityAnalyzer complexityAnalyzer,
                         LayerViolationDetector layerViolationDetector,
                         PerformanceRiskDetector performanceRiskDetector) {
        this.couplingAnalyzer = couplingAnalyzer;
        this.circularDependencyDetector = circularDependencyDetector;
        this.complexityAnalyzer = complexityAnalyzer;
        this.layerViolationDetector = layerViolationDetector;
        this.performanceRiskDetector = performanceRiskDetector;
    }

    public ArchitectureSnapshot compute(List<ClassInfo> classes, DependencyGraph graph) {
        ArchitectureSnapshot snapshot = new ArchitectureSnapshot();

        // Coupling
        snapshot.setCouplingScore(couplingAnalyzer.analyze(graph));
        snapshot.setCouplingHotspots(couplingAnalyzer.analyzePerClass(graph));

        // Circular dependencies
        List<List<String>> cycles = circularDependencyDetector.detect(graph);
        snapshot.setCircularDependencies(cycles.size());
        snapshot.setCircularPaths(cycles.stream()
                .map(cycle -> String.join(" → ", cycle))
                .collect(Collectors.toList()));

        // Complexity
        snapshot.setComplexityScore(complexityAnalyzer.analyzeAverage(classes));
        snapshot.setComplexMethods(complexityAnalyzer.analyzeComplexMethods(classes));

        // Layer violations
        List<String> violations = layerViolationDetector.detect(classes, graph);
        snapshot.setLayerViolations(violations.size());
        snapshot.setViolations(violations);

        // Performance risks
        List<String> risks = performanceRiskDetector.detect(classes);
        snapshot.setPerformanceRisks(risks.size());
        snapshot.setRisks(risks);

        // Graph data
        snapshot.setGraphData(graph.toMap());

        // Class-level details
        snapshot.setTotalClasses(classes.size());
        snapshot.setTotalMethods(classes.stream().mapToInt(c -> c.getMethods().size()).sum());
        snapshot.setClassDetails(buildClassDetails(classes));

        return snapshot;
    }

    private List<Map<String, Object>> buildClassDetails(List<ClassInfo> classes) {
        List<Map<String, Object>> details = new ArrayList<>();
        for (ClassInfo cls : classes) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("className", cls.getClassName());
            entry.put("packageName", cls.getPackageName() != null ? cls.getPackageName() : "");
            entry.put("type", cls.getType().name());
            entry.put("filePath", cls.getFilePath() != null ? cls.getFilePath() : "unknown");
            entry.put("dependencyCount", cls.getDependencies().size());
            entry.put("methodCount", cls.getMethods().size());
            entry.put("dependencies", cls.getDependencies());
            entry.put("annotations", cls.getAnnotations());

            // Per-method summary
            List<Map<String, Object>> methods = new ArrayList<>();
            for (var method : cls.getMethods()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", method.getName());
                m.put("complexity", method.getCyclomaticComplexity());
                m.put("parameterCount", method.getParameterCount());
                m.put("callCount", method.getMethodCalls().size());
                methods.add(m);
            }
            entry.put("methods", methods);

            details.add(entry);
        }
        return details;
    }
}
