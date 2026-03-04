package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.analyzer.JavaFileParser;
import com.archsentinel.analyzer.MethodInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComplexityAnalyzerTest {

    private ComplexityAnalyzer complexityAnalyzer;

    @BeforeEach
    void setUp() {
        complexityAnalyzer = new ComplexityAnalyzer();
    }

    @Test
    void shouldReturnZeroForEmptyClassList() {
        assertEquals(0.0, complexityAnalyzer.analyzeAverage(List.of()));
        assertEquals(0, complexityAnalyzer.analyzeMax(List.of()));
    }

    @Test
    void shouldReturnZeroForClassWithNoMethods() {
        ClassInfo cls = new ClassInfo();
        cls.setClassName("Empty");
        assertEquals(0.0, complexityAnalyzer.analyzeAverage(List.of(cls)));
    }

    @Test
    void shouldCalculateAverageComplexity() {
        ClassInfo cls = new ClassInfo();
        cls.setClassName("TestClass");

        MethodInfo m1 = new MethodInfo();
        m1.setName("simpleMethod");
        m1.setCyclomaticComplexity(1);

        MethodInfo m2 = new MethodInfo();
        m2.setName("complexMethod");
        m2.setCyclomaticComplexity(5);

        cls.setMethods(List.of(m1, m2));

        // average = (1+5)/2 = 3, normalized = 3/20 * 100 = 15
        double avg = complexityAnalyzer.analyzeAverage(List.of(cls));
        assertEquals(15.0, avg, 0.01);
    }

    @Test
    void shouldCalculateMaxComplexity() {
        ClassInfo cls = new ClassInfo();
        cls.setClassName("TestClass");

        MethodInfo m1 = new MethodInfo();
        m1.setCyclomaticComplexity(3);
        MethodInfo m2 = new MethodInfo();
        m2.setCyclomaticComplexity(7);
        MethodInfo m3 = new MethodInfo();
        m3.setCyclomaticComplexity(2);

        cls.setMethods(List.of(m1, m2, m3));

        assertEquals(7, complexityAnalyzer.analyzeMax(List.of(cls)));
    }

    @Test
    void shouldNormalizeComplexityScoreTo100() {
        ClassInfo cls = new ClassInfo();
        cls.setClassName("VeryComplexClass");
        List<MethodInfo> methods = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MethodInfo m = new MethodInfo();
            m.setCyclomaticComplexity(50); // way above threshold
            methods.add(m);
        }
        cls.setMethods(methods);

        assertEquals(100.0, complexityAnalyzer.analyzeAverage(List.of(cls)));
    }
}
