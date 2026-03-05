package com.archsentinel.scoring;

import com.archsentinel.metrics.ArchitectureSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthScoreCalculatorTest {

    private HealthScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        ScoringConfig config = new ScoringConfig();
        calculator = new HealthScoreCalculator(config);
    }

    @Test
    void shouldReturnPerfectScoreForPristineSnapshot() {
        ArchitectureSnapshot snapshot = new ArchitectureSnapshot();
        snapshot.setCouplingScore(0.0);
        snapshot.setCircularDependencies(0);
        snapshot.setComplexityScore(0.0);
        snapshot.setLayerViolations(0);
        snapshot.setPerformanceRisks(0);

        double score = calculator.calculate(snapshot);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void shouldReturnReducedScoreForIssues() {
        ArchitectureSnapshot snapshot = new ArchitectureSnapshot();
        snapshot.setCouplingScore(50.0);
        snapshot.setCircularDependencies(2);
        snapshot.setComplexityScore(40.0);
        snapshot.setLayerViolations(1);
        snapshot.setPerformanceRisks(1);

        double score = calculator.calculate(snapshot);
        assertTrue(score < 100.0, "Score should be less than 100 when there are issues");
        assertTrue(score >= 0.0, "Score should not be negative");
    }

    @Test
    void shouldReturnZeroOrPositiveForWorstCase() {
        ArchitectureSnapshot snapshot = new ArchitectureSnapshot();
        snapshot.setCouplingScore(100.0);
        snapshot.setCircularDependencies(10);
        snapshot.setComplexityScore(100.0);
        snapshot.setLayerViolations(10);
        snapshot.setPerformanceRisks(10);

        double score = calculator.calculate(snapshot);
        assertEquals(0.0, score, 0.01, "Worst case score should be 0");
    }

    @Test
    void shouldReturnCorrectRiskLevel() {
        assertEquals("HEALTHY", calculator.getRiskLevel(85));
        assertEquals("MODERATE", calculator.getRiskLevel(70));
        assertEquals("AT_RISK", calculator.getRiskLevel(50));
        assertEquals("CRITICAL", calculator.getRiskLevel(30));
    }

    @Test
    void shouldReturnHealthyForScoreAt80() {
        assertEquals("HEALTHY", calculator.getRiskLevel(80));
    }

    @Test
    void shouldReturnModerateForScoreAt60() {
        assertEquals("MODERATE", calculator.getRiskLevel(60));
    }
}
