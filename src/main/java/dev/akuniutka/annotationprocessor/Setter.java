package dev.akuniutka.annotationprocessor;

class Setter {
    private final Field field;

    Setter(Field field) {
        this.field = field;
    }

    String getName() {
        String fieldName = field.getName();
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    @Override
    public String toString() {
        return getName();
    }
}
