package com.archsentinel.diff;

import com.archsentinel.metrics.ArchitectureSnapshot;

public class ImpactReport {

    private double couplingDelta;
    private boolean newCircularDependency;
    private int newCircularCount;
    private double complexityDelta;
    private int newLayerViolations;
    private int newPerformanceRisks;
    private double healthScoreChange;
    private ArchitectureSnapshot baseSnapshot;
    private ArchitectureSnapshot prSnapshot;
    private String riskLevel;

    public ImpactReport() {
    }

    public double getCouplingDelta() {
        return couplingDelta;
    }

    public void setCouplingDelta(double couplingDelta) {
        this.couplingDelta = couplingDelta;
    }

    public boolean isNewCircularDependency() {
        return newCircularDependency;
    }

    public void setNewCircularDependency(boolean newCircularDependency) {
        this.newCircularDependency = newCircularDependency;
    }

    public int getNewCircularCount() {
        return newCircularCount;
    }

    public void setNewCircularCount(int newCircularCount) {
        this.newCircularCount = newCircularCount;
    }

    public double getComplexityDelta() {
        return complexityDelta;
    }

    public void setComplexityDelta(double complexityDelta) {
        this.complexityDelta = complexityDelta;
    }

    public int getNewLayerViolations() {
        return newLayerViolations;
    }

    public void setNewLayerViolations(int newLayerViolations) {
        this.newLayerViolations = newLayerViolations;
    }

    public int getNewPerformanceRisks() {
        return newPerformanceRisks;
    }

    public void setNewPerformanceRisks(int newPerformanceRisks) {
        this.newPerformanceRisks = newPerformanceRisks;
    }

    public double getHealthScoreChange() {
        return healthScoreChange;
    }

    public void setHealthScoreChange(double healthScoreChange) {
        this.healthScoreChange = healthScoreChange;
    }

    public ArchitectureSnapshot getBaseSnapshot() {
        return baseSnapshot;
    }

    public void setBaseSnapshot(ArchitectureSnapshot baseSnapshot) {
        this.baseSnapshot = baseSnapshot;
    }

    public ArchitectureSnapshot getPrSnapshot() {
        return prSnapshot;
    }

    public void setPrSnapshot(ArchitectureSnapshot prSnapshot) {
        this.prSnapshot = prSnapshot;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
