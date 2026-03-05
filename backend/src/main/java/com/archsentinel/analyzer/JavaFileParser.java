package com.archsentinel.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaFileParser {

    private static final Logger log = LoggerFactory.getLogger(JavaFileParser.class);

    public ClassInfo parse(File javaFile) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            ClassInfo classInfo = new ClassInfo();
            classInfo.setFilePath(javaFile.getAbsolutePath());

            cu.getPackageDeclaration().ifPresent(pkg -> classInfo.setPackageName(pkg.getNameAsString()));

            cu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(cls -> {
                classInfo.setClassName(cls.getNameAsString());

                // Extract annotations
                List<String> annotations = new ArrayList<>();
                cls.getAnnotations().forEach(ann -> annotations.add(ann.getNameAsString()));
                classInfo.setAnnotations(annotations);
                classInfo.setType(detectClassType(annotations));

                // Extract interfaces
                List<String> interfaces = new ArrayList<>();
                cls.getImplementedTypes().forEach(t -> interfaces.add(t.getNameAsString()));
                classInfo.setInterfaces(interfaces);

                // Extract superclass
                cls.getExtendedTypes().stream().findFirst()
                        .ifPresent(t -> classInfo.setSuperClass(t.getNameAsString()));

                // Extract field dependencies (@Autowired or field injection)
                List<String> deps = new ArrayList<>();
                cls.getFields().forEach(field -> {
                    if (isInjected(field)) {
                        field.getVariables().forEach(var ->
                                deps.add(field.getCommonType().asString()));
                    }
                });

                // Extract constructor injection
                cls.getConstructors().forEach(ctor -> extractConstructorDeps(ctor, deps));
                classInfo.setDependencies(deps);

                // Extract methods
                List<MethodInfo> methods = new ArrayList<>();
                cls.getMethods().forEach(method -> methods.add(parseMethod(method)));
                classInfo.setMethods(methods);
            });

            return classInfo;
        } catch (Exception e) {
            log.warn("Failed to parse {}: {}", javaFile.getName(), e.getMessage());
            return null;
        }
    }

    private ClassInfo.ClassType detectClassType(List<String> annotations) {
        for (String ann : annotations) {
            switch (ann) {
                case "RestController":
                case "Controller":
                    return ClassInfo.ClassType.CONTROLLER;
                case "Service":
                    return ClassInfo.ClassType.SERVICE;
                case "Repository":
                    return ClassInfo.ClassType.REPOSITORY;
                case "Entity":
                    return ClassInfo.ClassType.ENTITY;
                case "Component":
                    return ClassInfo.ClassType.COMPONENT;
                default:
                    break;
            }
        }
        return ClassInfo.ClassType.UNKNOWN;
    }

    private boolean isInjected(FieldDeclaration field) {
        return field.getAnnotations().stream()
                .anyMatch(ann -> "Autowired".equals(ann.getNameAsString()));
    }

    private void extractConstructorDeps(ConstructorDeclaration ctor, List<String> deps) {
        if (ctor.getParameters().size() > 1 ||
                ctor.getAnnotations().stream().anyMatch(a -> "Autowired".equals(a.getNameAsString()))) {
            ctor.getParameters().forEach(param -> {
                String typeName = param.getType().asString();
                if (!deps.contains(typeName)) {
                    deps.add(typeName);
                }
            });
        } else if (ctor.getParameters().size() == 1) {
            ctor.getParameters().forEach(param -> {
                String typeName = param.getType().asString();
                if (!deps.contains(typeName)) {
                    deps.add(typeName);
                }
            });
        }
    }

    private MethodInfo parseMethod(MethodDeclaration method) {
        MethodInfo info = new MethodInfo();
        info.setName(method.getNameAsString());
        info.setReturnType(method.getType().asString());
        info.setParameterCount(method.getParameters().size());

        List<String> annotations = new ArrayList<>();
        method.getAnnotations().forEach(ann -> annotations.add(ann.getNameAsString()));
        info.setAnnotations(annotations);

        List<String> calls = new ArrayList<>();
        method.findAll(MethodCallExpr.class).forEach(call -> calls.add(call.getNameAsString()));
        info.setMethodCalls(calls);

        info.setCyclomaticComplexity(computeComplexity(method));
        return info;
    }

    int computeComplexity(MethodDeclaration method) {
        int[] complexity = {1};
        method.findAll(IfStmt.class).forEach(s -> complexity[0]++);
        method.findAll(ForStmt.class).forEach(s -> complexity[0]++);
        method.findAll(ForEachStmt.class).forEach(s -> complexity[0]++);
        method.findAll(WhileStmt.class).forEach(s -> complexity[0]++);
        method.findAll(DoStmt.class).forEach(s -> complexity[0]++);
        method.findAll(SwitchEntry.class).forEach(s -> {
            if (!s.getLabels().isEmpty()) complexity[0]++;
        });
        method.findAll(CatchClause.class).forEach(s -> complexity[0]++);
        method.findAll(BinaryExpr.class).forEach(expr -> {
            if (expr.getOperator() == BinaryExpr.Operator.AND ||
                    expr.getOperator() == BinaryExpr.Operator.OR) {
                complexity[0]++;
            }
        });
        method.findAll(ConditionalExpr.class).forEach(e -> complexity[0]++);
        return complexity[0];
    }
}
