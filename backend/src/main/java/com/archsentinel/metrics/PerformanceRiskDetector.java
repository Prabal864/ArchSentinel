package com.archsentinel.metrics;

import com.archsentinel.analyzer.ClassInfo;
import com.archsentinel.analyzer.MethodInfo;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PerformanceRiskDetector {

    private static final Logger log = LoggerFactory.getLogger(PerformanceRiskDetector.class);

    public List<String> detect(List<ClassInfo> classes) {
        List<String> risks = new ArrayList<>();

        Set<String> repositoryClasses = classes.stream()
                .filter(c -> c.getType() == ClassInfo.ClassType.REPOSITORY)
                .map(ClassInfo::getClassName)
                .collect(Collectors.toSet());

        for (ClassInfo cls : classes) {
            if (cls.getFilePath() == null) continue;

            try {
                File file = new File(cls.getFilePath());
                if (!file.exists()) continue;

                CompilationUnit cu = StaticJavaParser.parse(file);
                cu.findAll(MethodDeclaration.class).forEach(method -> {
                    detectLoopRisks(method, cls.getClassName(), repositoryClasses, risks);
                });
            } catch (Exception e) {
                log.debug("Could not analyze {} for performance risks: {}", cls.getClassName(), e.getMessage());
                // fallback: check method calls without AST
                detectFromMethodInfo(cls, repositoryClasses, risks);
            }
        }

        return risks;
    }

    private void detectLoopRisks(MethodDeclaration method, String className,
                                  Set<String> repositoryClasses, List<String> risks) {
        // Check for-loops
        method.findAll(ForStmt.class).forEach(loop ->
                loop.findAll(MethodCallExpr.class).forEach(call ->
                        checkCallInLoop(call, className, method.getNameAsString(), "for-loop", repositoryClasses, risks)));

        method.findAll(ForEachStmt.class).forEach(loop ->
                loop.findAll(MethodCallExpr.class).forEach(call ->
                        checkCallInLoop(call, className, method.getNameAsString(), "for-each-loop", repositoryClasses, risks)));

        method.findAll(WhileStmt.class).forEach(loop ->
                loop.findAll(MethodCallExpr.class).forEach(call ->
                        checkCallInLoop(call, className, method.getNameAsString(), "while-loop", repositoryClasses, risks)));

        method.findAll(DoStmt.class).forEach(loop ->
                loop.findAll(MethodCallExpr.class).forEach(call ->
                        checkCallInLoop(call, className, method.getNameAsString(), "do-while-loop", repositoryClasses, risks)));
    }

    private void checkCallInLoop(MethodCallExpr call, String className, String methodName,
                                  String loopType, Set<String> repositoryClasses, List<String> risks) {
        String callName = call.getNameAsString();
        // Check for common repository method patterns
        boolean isDbCall = call.getScope().map(scope -> {
            String scopeName = scope.toString();
            return repositoryClasses.stream().anyMatch(r ->
                    scopeName.toLowerCase().contains(r.toLowerCase()) ||
                    r.toLowerCase().contains(scopeName.toLowerCase()));
        }).orElse(false);

        if (isDbCall || isCommonDbMethod(callName)) {
            String risk = String.format("PERFORMANCE_RISK: Potential N+1 query in %s.%s() - DB call '%s' inside %s",
                    className, methodName, callName, loopType);
            if (!risks.contains(risk)) {
                risks.add(risk);
            }
        }
    }

    private boolean isCommonDbMethod(String methodName) {
        return methodName.equals("findById") || methodName.equals("findAll") ||
               methodName.equals("save") || methodName.equals("delete") ||
               methodName.equals("findBy") || methodName.startsWith("findBy") ||
               methodName.equals("count") || methodName.equals("existsById");
    }

    private void detectFromMethodInfo(ClassInfo cls, Set<String> repositoryClasses, List<String> risks) {
        for (MethodInfo method : cls.getMethods()) {
            boolean hasLoop = method.getMethodCalls().stream()
                    .anyMatch(c -> c.contains("forEach") || c.contains("stream"));
            boolean hasDbCall = method.getMethodCalls().stream()
                    .anyMatch(this::isCommonDbMethod);
            if (hasLoop && hasDbCall) {
                risks.add(String.format("PERFORMANCE_RISK: Potential N+1 query in %s.%s()",
                        cls.getClassName(), method.getName()));
            }
        }
    }
}
