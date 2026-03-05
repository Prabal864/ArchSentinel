package com.archsentinel.diff;

import com.archsentinel.metrics.ArchitectureSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiffEngineTest {

    private DiffEngine diffEngine;

    @BeforeEach
    void setUp() {
        diffEngine = new DiffEngine();
    }

    @Test
    void shouldComputeHealthScoreDelta() {
        ArchitectureSnapshot base = new ArchitectureSnapshot();
        base.setHealthScore(80.0);

        ArchitectureSnapshot pr = new ArchitectureSnapshot();
        pr.setHealthScore(65.0);

        ImpactReport report = diffEngine.compare(base, pr);

        assertEquals(-15.0, report.getHealthScoreChange(), 0.01);
    }

    @Test
    void shouldDetectNewCircularDependencies() {
        ArchitectureSnapshot base = new ArchitectureSnapshot();
        base.setCircularDependencies(0);
        base.setHealthScore(85.0);

        ArchitectureSnapshot pr = new ArchitectureSnapshot();
        pr.setCircularDependencies(2);
        pr.setHealthScore(70.0);

        ImpactReport report = diffEngine.compare(base, pr);

        assertTrue(report.isNewCircularDependency());
        assertEquals(2, report.getNewCircularCount());
    }

    @Test
    void shouldNotReportNegativeNewViolations() {
        ArchitectureSnapshot base = new ArchitectureSnapshot();
        base.setLayerViolations(3);
        base.setHealthScore(60.0);

        ArchitectureSnapshot pr = new ArchitectureSnapshot();
        pr.setLayerViolations(1);
        pr.setHealthScore(70.0);

        ImpactReport report = diffEngine.compare(base, pr);

        assertEquals(0, report.getNewLayerViolations(), "New violations should not be negative");
    }

    @Test
    void shouldAssignCorrectRiskLevel() {
        ArchitectureSnapshot base = new ArchitectureSnapshot();
        base.setHealthScore(85.0);

        ArchitectureSnapshot pr = new ArchitectureSnapshot();
        pr.setHealthScore(85.0);

        ImpactReport report = diffEngine.compare(base, pr);
        assertEquals("LOW", report.getRiskLevel());
    }

    @Test
    void shouldAssignCriticalRiskLevel() {
        ArchitectureSnapshot base = new ArchitectureSnapshot();
        base.setHealthScore(80.0);

        ArchitectureSnapshot pr = new ArchitectureSnapshot();
        pr.setHealthScore(20.0);

        ImpactReport report = diffEngine.compare(base, pr);
        assertEquals("CRITICAL", report.getRiskLevel());
    }

    @Test
    void shouldComputeCouplingDelta() {
        ArchitectureSnapshot base = new ArchitectureSnapshot();
        base.setCouplingScore(30.0);
        base.setHealthScore(75.0);

        ArchitectureSnapshot pr = new ArchitectureSnapshot();
        pr.setCouplingScore(50.0);
        pr.setHealthScore(60.0);

        ImpactReport report = diffEngine.compare(base, pr);
        assertEquals(20.0, report.getCouplingDelta(), 0.01);
    }
}
