package dev.akuniutka.annotationprocessor;

import java.util.LinkedHashSet;
import java.util.Set;

public class Class {
    private String name;
    private String packageName;
    private String classSimpleName;
    private String className;
    private final Set<Field> fields = new LinkedHashSet<>();

    public String getQualifiedName() {
        return (packageName == null ? "" : packageName + ".") + name;
    }

    public void addField(Field field) {
        if (fields.isEmpty()) {
            className = field.getClassName();
            int lastDot = className.lastIndexOf('.');
            if (lastDot > 0) {
                packageName = className.substring(0, lastDot);
            } else {
                packageName = null;
            }
            classSimpleName = className.substring(lastDot + 1);
            name = classSimpleName + "Builder";
        } else if (!className.equals(field.getClassName())) {
            throw new IllegalArgumentException("adding field to wrong class");
        }
        fields.add(field);
    }

    public Class merge(Class anotherClass) {
        for (Field field : anotherClass.fields) {
            if (fields.contains(field)) {
                throw new IllegalArgumentException("duplicate field " + field.getClassName() + ":" + field.getName());
            }
            fields.add(field);
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (packageName != null) {
            result.append("package ").append(packageName).append(";\n\n");
        }
        result
                .append("public class ").append(name).append(" {\n")
                .append(String.format("    private %s object = new %s();%n%n", classSimpleName, classSimpleName))
                .append("    public ").append(classSimpleName).append(" build() {\n")
                .append("        return object;\n")
                .append("    }\n");
        for (Field field : fields) {
            Setter setter = new Setter(field);
            result.append(String.format("%n    public %s %s(%s value) {%n", name, setter, field.getType()))
                    .append("        object.").append(setter).append("(value);\n")
                    .append("        return this;\n")
                    .append("    }\n");
        }
        result.append("}\n");
        return result.toString();
    }
}
