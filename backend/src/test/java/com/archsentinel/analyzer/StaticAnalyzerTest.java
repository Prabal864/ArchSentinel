package com.archsentinel.analyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaticAnalyzerTest {

    private StaticAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new StaticAnalyzer(new JavaFileParser());
    }

    @Test
    void shouldAnalyzeSampleJavaDirectory() {
        URL resource = getClass().getClassLoader().getResource("sample-java");
        assertNotNull(resource, "Sample java directory not found");
        Path sampleDir = Paths.get(resource.getPath());

        List<ClassInfo> classes = analyzer.analyze(sampleDir);

        assertFalse(classes.isEmpty(), "Should find at least one class");
        assertTrue(classes.size() >= 4, "Should find at least 4 sample classes");
    }

    @Test
    void shouldDetectClassTypes() {
        URL resource = getClass().getClassLoader().getResource("sample-java");
        assertNotNull(resource);
        Path sampleDir = Paths.get(resource.getPath());

        List<ClassInfo> classes = analyzer.analyze(sampleDir);

        assertTrue(classes.stream().anyMatch(c -> c.getType() == ClassInfo.ClassType.CONTROLLER),
                "Should detect Controller class");
        assertTrue(classes.stream().anyMatch(c -> c.getType() == ClassInfo.ClassType.SERVICE),
                "Should detect Service class");
        assertTrue(classes.stream().anyMatch(c -> c.getType() == ClassInfo.ClassType.REPOSITORY),
                "Should detect Repository class");
    }

    @Test
    void shouldReturnEmptyListForNonExistentDir() {
        List<ClassInfo> classes = analyzer.analyze(Paths.get("/nonexistent/path"));
        assertTrue(classes.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForNullDir() {
        List<ClassInfo> classes = analyzer.analyze(null);
        assertTrue(classes.isEmpty());
    }
}
