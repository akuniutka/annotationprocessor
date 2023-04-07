package dev.akuniutka.annotationprocessor;

public class Field {
    private final String name;
    private final String type;

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

    public String getSetterName() {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
