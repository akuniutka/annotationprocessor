package dev.akuniutka.annotationprocessor;

public class Setter {
    private final Field field;

    public Setter(Field field) {
        this.field = field;
    }

    public String getName() {
        String fieldName = field.getName();
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    @Override
    public String toString() {
        return getName();
    }
}
