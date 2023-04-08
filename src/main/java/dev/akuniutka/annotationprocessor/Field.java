package dev.akuniutka.annotationprocessor;

public class Field implements Cloneable {
    private String name;
    private String type;

    public Field(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public Field clone() {
        final Field clone;
        try {
            clone = (Field) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("superclass mess up", e);
        }
        clone.name = name;
        clone.type = type;
        return clone;
    }
}
