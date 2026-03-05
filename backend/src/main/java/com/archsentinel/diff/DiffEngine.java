package com.archsentinel.diff;

import com.archsentinel.metrics.ArchitectureSnapshot;
import org.springframework.stereotype.Service;

@Service
public class DiffEngine {

    public ImpactReport compare(ArchitectureSnapshot base, ArchitectureSnapshot pr) {
        ImpactReport report = new ImpactReport();
        report.setBaseSnapshot(base);
        report.setPrSnapshot(pr);

        report.setCouplingDelta(pr.getCouplingScore() - base.getCouplingScore());
        report.setComplexityDelta(pr.getComplexityScore() - base.getComplexityScore());

        int circularDelta = pr.getCircularDependencies() - base.getCircularDependencies();
        report.setNewCircularCount(Math.max(0, circularDelta));
        report.setNewCircularDependency(circularDelta > 0);

        report.setNewLayerViolations(Math.max(0, pr.getLayerViolations() - base.getLayerViolations()));
        report.setNewPerformanceRisks(Math.max(0, pr.getPerformanceRisks() - base.getPerformanceRisks()));
        report.setHealthScoreChange(pr.getHealthScore() - base.getHealthScore());

        report.setRiskLevel(computeRiskLevel(report));
        return report;
    }

    private String computeRiskLevel(ImpactReport report) {
        double prScore = report.getPrSnapshot().getHealthScore();
        double change = report.getHealthScoreChange();

        if (prScore >= 80 && change >= -5) {
            return "LOW";
        } else if (prScore >= 60 || change >= -10) {
            return "MEDIUM";
        } else if (prScore >= 40 || change >= -20) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }
}
