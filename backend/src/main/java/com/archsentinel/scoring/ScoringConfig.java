package com.archsentinel.scoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "archsentinel.scoring")
public class ScoringConfig {

    private double couplingWeight = 0.25;
    private double circularWeight = 0.25;
    private double complexityWeight = 0.20;
    private double performanceWeight = 0.20;
    private double layerViolationWeight = 0.10;

    public double getCouplingWeight() {
        return couplingWeight;
    }

    public void setCouplingWeight(double couplingWeight) {
        this.couplingWeight = couplingWeight;
    }

    public double getCircularWeight() {
        return circularWeight;
    }

    public void setCircularWeight(double circularWeight) {
        this.circularWeight = circularWeight;
    }

    public double getComplexityWeight() {
        return complexityWeight;
    }

    public void setComplexityWeight(double complexityWeight) {
        this.complexityWeight = complexityWeight;
    }

    public double getPerformanceWeight() {
        return performanceWeight;
    }

    public void setPerformanceWeight(double performanceWeight) {
        this.performanceWeight = performanceWeight;
    }

    public double getLayerViolationWeight() {
        return layerViolationWeight;
    }

    public void setLayerViolationWeight(double layerViolationWeight) {
        this.layerViolationWeight = layerViolationWeight;
    }
}
