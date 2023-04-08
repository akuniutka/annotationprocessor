package dev.akuniutka.annotationprocessor;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

class Builder {
    private final Set<Field> fields = new LinkedHashSet<>();

    void addFieldFromElement(Element element) {
        String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
        String fieldName = element.getSimpleName().toString();
        String fieldType = element.asType().toString();
        Field field = new Field(className, fieldName, fieldType);
        if (fields.contains(field)) {
            throw new IllegalArgumentException("duplicate field " + className + ":" + fieldName);
        }
        fields.add(field);
    }

    void merge(Builder anotherBuilder) {
        for (Field field : anotherBuilder.fields) {
            if (fields.contains(field)) {
                throw new IllegalArgumentException("duplicate field " + field.getClassName() + ":" + field.getName());
            }
            fields.add(field);
        }
    }

    Collection<Class> getClasses() {
        return fields.stream().collect(Collectors.groupingBy(
                Field::getClassName,
                Collector.of(Class::new, Class::addField, Class::merge)
        )).values();
    }
}
