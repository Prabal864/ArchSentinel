package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.analyzer.MethodInfo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ComplexityAnalyzer {

    private static final int COMPLEXITY_THRESHOLD = 5;

    public double analyzeAverage(List<ClassInfo> classes) {
        if (classes.isEmpty()) return 0.0;

        int totalComplexity = 0;
        int methodCount = 0;

        for (ClassInfo cls : classes) {
            for (MethodInfo method : cls.getMethods()) {
                totalComplexity += method.getCyclomaticComplexity();
                methodCount++;
            }
        }

        if (methodCount == 0) return 0.0;

        double avg = (double) totalComplexity / methodCount;
        // Normalize: assume max reasonable average complexity is 20
        return Math.min(100.0, (avg / 20.0) * 100.0);
    }

    public int analyzeMax(List<ClassInfo> classes) {
        return classes.stream()
                .flatMap(cls -> cls.getMethods().stream())
                .mapToInt(MethodInfo::getCyclomaticComplexity)
                .max()
                .orElse(0);
    }

    /**
     * Returns methods with cyclomatic complexity >= threshold, sorted by complexity descending.
     * Each map contains: className, methodName, complexity, parameterCount, filePath
     */
    public List<Map<String, Object>> analyzeComplexMethods(List<ClassInfo> classes) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClassInfo cls : classes) {
            for (MethodInfo method : cls.getMethods()) {
                if (method.getCyclomaticComplexity() >= COMPLEXITY_THRESHOLD) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("className", cls.getClassName());
                    entry.put("classType", cls.getType().name());
                    entry.put("methodName", method.getName());
                    entry.put("complexity", method.getCyclomaticComplexity());
                    entry.put("parameterCount", method.getParameterCount());
                    entry.put("filePath", cls.getFilePath() != null ? cls.getFilePath() : "unknown");

                    String severity;
                    int c = method.getCyclomaticComplexity();
                    if (c >= 20) severity = "CRITICAL";
                    else if (c >= 10) severity = "HIGH";
                    else if (c >= 7) severity = "MEDIUM";
                    else severity = "LOW";
                    entry.put("severity", severity);

                    result.add(entry);
                }
            }
        }

        result.sort((a, b) -> Integer.compare(
                (int) b.get("complexity"), (int) a.get("complexity")));

        return result;
    }
}
