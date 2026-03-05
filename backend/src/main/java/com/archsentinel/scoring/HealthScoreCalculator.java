package com.archsentinel.scoring;

import com.archsentinel.metrics.ArchitectureSnapshot;
import org.springframework.stereotype.Service;

@Service
public class HealthScoreCalculator {

    private final ScoringConfig config;

    public HealthScoreCalculator(ScoringConfig config) {
        this.config = config;
    }

    public double calculate(ArchitectureSnapshot snapshot) {
        double couplingNorm = Math.min(100.0, snapshot.getCouplingScore());
        double circularNorm = Math.min(100.0, snapshot.getCircularDependencies() * 25.0);
        double complexityNorm = Math.min(100.0, snapshot.getComplexityScore());
        double performanceNorm = Math.min(100.0, snapshot.getPerformanceRisks() * 20.0);
        double layerNorm = Math.min(100.0, snapshot.getLayerViolations() * 20.0);

        double penalty =
                couplingNorm * config.getCouplingWeight() +
                circularNorm * config.getCircularWeight() +
                complexityNorm * config.getComplexityWeight() +
                performanceNorm * config.getPerformanceWeight() +
                layerNorm * config.getLayerViolationWeight();

        return Math.max(0.0, Math.round((100.0 - penalty) * 100.0) / 100.0);
    }

    public String getRiskLevel(double healthScore) {
        if (healthScore >= 80) return "HEALTHY";
        if (healthScore >= 60) return "MODERATE";
        if (healthScore >= 40) return "AT_RISK";
        return "CRITICAL";
    }
}
