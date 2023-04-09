package dev.akuniutka.annotationprocessor;

import java.util.Objects;

class Field {
    private final String className;
    private final String name;
    private final String type;

    Field(String className, String name, String type) {
        this.className = className;
        this.name = name;
        this.type = type;
    }

    String getClassName() {
        return className;
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return className.equals(field.className) && name.equals(field.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name);
    }
}
