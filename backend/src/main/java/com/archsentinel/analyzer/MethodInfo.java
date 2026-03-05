package com.archsentinel.analyzer;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {

    private String name;
    private String returnType;
    private int parameterCount;
    private List<String> annotations;
    private List<String> methodCalls;
    private int cyclomaticComplexity;

    public MethodInfo() {
        this.annotations = new ArrayList<>();
        this.methodCalls = new ArrayList<>();
        this.cyclomaticComplexity = 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<String> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(List<String> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    @Override
    public String toString() {
        return "MethodInfo{name='" + name + "', complexity=" + cyclomaticComplexity + "}";
    }
}
