package dev.akuniutka.annotationprocessor;

import java.util.LinkedHashSet;
import java.util.Set;

class Class {
    private String name;
    private String className;
    private String packageName;
    private String classSimpleName;
    private final Set<Field> fields = new LinkedHashSet<>();

    String getQualifiedName() {
        return (packageName == null ? "" : packageName + ".") + name;
    }

    void addField(Field field) {
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

    Class merge(Class anotherClass) {
        if (!anotherClass.fields.isEmpty()) {
            if (fields.isEmpty()) {
                name = anotherClass.name;
                className = anotherClass.className;
                packageName = anotherClass.packageName;
                classSimpleName = anotherClass.classSimpleName;
            } else if (!className.equals(anotherClass.className)) {
                throw new IllegalArgumentException("parts being merged belong to different classes");
            } else if (anotherClass.fields.stream().anyMatch(fields::contains)) {
                throw new IllegalArgumentException("fields duplicates found");
            }
            fields.addAll(anotherClass.fields);
        }
        return this;
    }

    String getCode() {
        StringBuilder result = new StringBuilder();
        if (packageName != null) {
            result.append("package ").append(packageName).append(";\n\n");
        }
        result.append("public class ").append(name).append(" {\n");
        result.append(String.format("    private %s object = new %s();%n%n", classSimpleName, classSimpleName));
        result.append("    public ").append(classSimpleName).append(" build() {\n");
        result.append("        return object;\n");
        result.append("    }\n");
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
