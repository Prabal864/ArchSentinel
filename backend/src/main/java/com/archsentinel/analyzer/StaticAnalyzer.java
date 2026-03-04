package com.archsentinel.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class StaticAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(StaticAnalyzer.class);

    private final JavaFileParser parser;

    public StaticAnalyzer(JavaFileParser parser) {
        this.parser = parser;
    }

    public List<ClassInfo> analyze(Path rootDir) {
        List<ClassInfo> classes = new ArrayList<>();
        if (rootDir == null || !Files.exists(rootDir)) {
            log.warn("Root directory does not exist: {}", rootDir);
            return classes;
        }

        try (Stream<Path> paths = Files.walk(rootDir)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        ClassInfo info = parser.parse(javaFile.toFile());
                        if (info != null && info.getClassName() != null) {
                            classes.add(info);
                        }
                    });
        } catch (Exception e) {
            log.error("Error scanning directory {}: {}", rootDir, e.getMessage());
        }

        log.info("Analyzed {} Java classes in {}", classes.size(), rootDir);
        return classes;
    }
}
