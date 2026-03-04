package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.analyzer.StaticAnalyzer;
import com.archsentinel.analyzer.JavaFileParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceRiskDetectorTest {

    private PerformanceRiskDetector detector;
    private StaticAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        detector = new PerformanceRiskDetector();
        analyzer = new StaticAnalyzer(new JavaFileParser());
    }

    @Test
    void shouldDetectDbCallInLoopFromSampleFiles() {
        URL resource = getClass().getClassLoader().getResource("sample-java");
        assertNotNull(resource);

        List<ClassInfo> classes = analyzer.analyze(Paths.get(resource.getPath()));
        assertFalse(classes.isEmpty());

        List<String> risks = detector.detect(classes);
        // OrderService.processAllOrders() has a repository call inside a for loop
        assertFalse(risks.isEmpty(), "Should detect performance risk in OrderService.processAllOrders()");
    }

    @Test
    void shouldReturnEmptyRisksForNoClasses() {
        List<String> risks = detector.detect(List.of());
        assertTrue(risks.isEmpty());
    }

    @Test
    void shouldReturnEmptyRisksForNonRepositoryClasses() {
        ClassInfo service = new ClassInfo();
        service.setClassName("SimpleService");
        service.setType(ClassInfo.ClassType.SERVICE);

        List<String> risks = detector.detect(List.of(service));
        assertTrue(risks.isEmpty());
    }
}
