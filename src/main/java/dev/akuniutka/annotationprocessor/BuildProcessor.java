package dev.akuniutka.annotationprocessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("dev.akuniutka.annotationprocessor.BuilderField")
@SupportedSourceVersion(SourceVersion.RELEASE_0)
public class BuildProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(BuilderField.class);
        if (fields.size() > 0) {
             Map<String, Map<String, String>> fieldsMap = fields.stream().collect(Collectors.groupingBy(
                     field -> ((TypeElement) field.getEnclosingElement()).getQualifiedName().toString(),
                     Collectors.toMap(
                             field -> field.getSimpleName().toString(),
                             field -> field.asType().toString()
                     )
             ));

            try {
               createBuilderClasses(fieldsMap);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            return true;
        } else {
            return false;
        }
    }

    private void createBuilderClasses(Map<String, Map<String, String>> fieldsMap) throws IOException {
        for (Map.Entry<String, Map<String, String>> entry : fieldsMap.entrySet()) {
            createBuilderClassNew(entry.getKey(), entry.getValue());
        }
    }

    private void createBuilderClassNew(String className, Map<String, String> fields) throws IOException {
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }
        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + "Builder";
        String builderSimpleClassName = builderClassName.substring(lastDot + 1);
        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            if (packageName != null) {
                out.printf("package %s;%n%n", packageName);
            }
            out.printf("public class %s {%n", builderSimpleClassName);
            out.printf("    private %s object = new %s();%n%n", simpleClassName, simpleClassName);
            out.printf("    public %s build() {%n        return object;%n    }%n", simpleClassName);
            fields.forEach((fieldName, fieldType) -> {
                out.printf("%n    public %s set%s(%s value) {%n", builderSimpleClassName, capitalize(fieldName), fieldType);
                out.printf("        object.%s = value;%n        return this;%n    }%n", fieldName);
            });
            out.println('}');
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
