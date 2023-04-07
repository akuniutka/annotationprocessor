package dev.akuniutka.annotationprocessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("dev.akuniutka.annotationprocessor.BuilderField")
@SupportedSourceVersion(SourceVersion.RELEASE_0)
public class BuildProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(BuilderField.class);
        Map<Boolean, List<Element>> annotatedMethods = elementsAnnotatedWith.stream()
                .collect(Collectors.partitioningBy(
                        element -> ((ExecutableType) element.asType()).getParameterTypes().size() == 1
                                        && element.getSimpleName().toString().startsWith("set")

                ));
        List<Element> setters = annotatedMethods.get(true);
        if (setters.size() > 0) {
            Map<String, Map<String, String>> settersMap = setters.stream().collect(Collectors.groupingBy(
                    element -> ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString(),
                    Collectors.toMap(
                            setter -> setter.getSimpleName().toString(),
                            setter -> ((ExecutableType) setter.asType()).getParameterTypes().get(0).toString()
                    )
            ));

            try {
               createBuilderClassesNew(settersMap);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            return true;
        } else {
            return false;
        }
    }

    private void createBuilderClassesNew(Map<String, Map<String, String>> settersMap) throws IOException {
        for (Map.Entry<String, Map<String, String>> entry : settersMap.entrySet()) {
            createBuilderClass(entry.getKey(), entry.getValue());
        }
    }

    private void createBuilderClass(String className, Map<String, String> settersMap) throws IOException {
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
            settersMap.forEach((methodName, argumentType) -> {
                out.printf("%n    public %s %s(%s value) {%n", builderSimpleClassName, methodName, argumentType);
                out.printf("        object.%s(value);%n        return this;%n    }%n", methodName);

            });
            out.println('}');
        }
    }
}
