package dev.akuniutka.annotationprocessor;

import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

public class ClassBuilder {
    private final String name;
    private final String packageName;
    private final String originalClassName;
    private final List<Field> fields;

    public ClassBuilder(String classQuilifiedName) {
        fields = new ArrayList<>();
        int lastDot = classQuilifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = classQuilifiedName.substring(0, lastDot);
        } else {
            packageName = null;
        }
        this.originalClassName = classQuilifiedName.substring(lastDot + 1);
        name = this.originalClassName + "Builder";
    }

    public ClassBuilder(String originalClassName, List<Field> fields) {
        this(originalClassName);
        fields.forEach(field -> this.fields.add(field.clone()));
    }

    public String getQualifiedName() {
        return (packageName == null ? "" : packageName + ".") + name;
    }

    public void build(PrintWriter out) {
        if (packageName != null) {
            out.printf("package %s;%n%n", packageName);
        }
        out.printf("public class %s {%n", name);
        out.printf("    private %s object = new %s();%n%n", originalClassName, originalClassName);
        out.printf("    public %s build() {%n        return object;%n    }%n", originalClassName);
        for (Field field : fields) {
            Setter setter = new Setter(field);
            out.printf("%n    public %s %s(%s value) {%n", name, setter, field.getType());
            out.printf("        object.%s(value);%n", setter);
            out.printf("        return this;%n    }%n");
        }
        out.println('}');
    }
}
