package com.archsentinel.analyzer;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {

    public enum ClassType {
        CONTROLLER, SERVICE, REPOSITORY, ENTITY, COMPONENT, UNKNOWN
    }

    private String className;
    private String packageName;
    private ClassType type;
    private List<String> annotations;
    private List<String> dependencies;
    private List<MethodInfo> methods;
    private List<String> interfaces;
    private String superClass;
    private String filePath;

    public ClassInfo() {
        this.type = ClassType.UNKNOWN;
        this.annotations = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.interfaces = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ClassType getType() {
        return type;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFullName() {
        if (packageName != null && !packageName.isEmpty()) {
            return packageName + "." + className;
        }
        return className;
    }

    @Override
    public String toString() {
        return "ClassInfo{className='" + className + "', type=" + type + ", deps=" + dependencies.size() + "}";
    }
}
