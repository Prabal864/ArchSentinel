package com.archsentinel.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchitectureSnapshot {

    private double couplingScore;
    private int circularDependencies;
    private double complexityScore;
    private int layerViolations;
    private int performanceRisks;
    private double healthScore;
    private List<String> circularPaths;
    private List<String> violations;
    private List<String> risks;
    private Map<String, Object> graphData;

    // === NEW: Pinpointed detail fields ===
    private List<Map<String, Object>> couplingHotspots;     // per-class coupling (fanIn, fanOut)
    private List<Map<String, Object>> complexMethods;        // methods with high cyclomatic complexity
    private List<Map<String, Object>> classDetails;          // per-class info (name, type, file, deps, methodCount)
    private int totalClasses;
    private int totalMethods;

    public ArchitectureSnapshot() {
        this.circularPaths = new ArrayList<>();
        this.violations = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.graphData = new HashMap<>();
        this.couplingHotspots = new ArrayList<>();
        this.complexMethods = new ArrayList<>();
        this.classDetails = new ArrayList<>();
    }

    // ...existing getters/setters...
    public double getCouplingScore() {
        return couplingScore;
    }

    public void setCouplingScore(double couplingScore) {
        this.couplingScore = couplingScore;
    }

    public int getCircularDependencies() {
        return circularDependencies;
    }

    public void setCircularDependencies(int circularDependencies) {
        this.circularDependencies = circularDependencies;
    }

    public double getComplexityScore() {
        return complexityScore;
    }

    public void setComplexityScore(double complexityScore) {
        this.complexityScore = complexityScore;
    }

    public int getLayerViolations() {
        return layerViolations;
    }

    public void setLayerViolations(int layerViolations) {
        this.layerViolations = layerViolations;
    }

    public int getPerformanceRisks() {
        return performanceRisks;
    }

    public void setPerformanceRisks(int performanceRisks) {
        this.performanceRisks = performanceRisks;
    }

    public double getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(double healthScore) {
        this.healthScore = healthScore;
    }

    public List<String> getCircularPaths() {
        return circularPaths;
    }

    public void setCircularPaths(List<String> circularPaths) {
        this.circularPaths = circularPaths;
    }

    public List<String> getViolations() {
        return violations;
    }

    public void setViolations(List<String> violations) {
        this.violations = violations;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public Map<String, Object> getGraphData() {
        return graphData;
    }

    public void setGraphData(Map<String, Object> graphData) {
        this.graphData = graphData;
    }

    // === NEW getters/setters ===

    public List<Map<String, Object>> getCouplingHotspots() {
        return couplingHotspots;
    }

    public void setCouplingHotspots(List<Map<String, Object>> couplingHotspots) {
        this.couplingHotspots = couplingHotspots;
    }

    public List<Map<String, Object>> getComplexMethods() {
        return complexMethods;
    }

    public void setComplexMethods(List<Map<String, Object>> complexMethods) {
        this.complexMethods = complexMethods;
    }

    public List<Map<String, Object>> getClassDetails() {
        return classDetails;
    }

    public void setClassDetails(List<Map<String, Object>> classDetails) {
        this.classDetails = classDetails;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getTotalMethods() {
        return totalMethods;
    }

    public void setTotalMethods(int totalMethods) {
        this.totalMethods = totalMethods;
    }
}
