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
            String className = ((TypeElement) setters.get(0).getEnclosingElement()).getQualifiedName().toString();
            Map<String, String> setterMap = setters.stream().collect(Collectors.toMap(
                    setter -> setter.getSimpleName().toString(),
                    setter -> ((ExecutableType) setter.asType()).getParameterTypes().get(0).toString()
            ));

            try {
                createBuilderClass(className, setterMap);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            return true;
        } else {
            return false;
        }
    }

    private void createBuilderClass(String classMame, Map<String, String> settersMap) throws IOException {
        String packageName = null;
        int lastDot = classMame.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = classMame.substring(0, lastDot);
        }
        String simpleClassName = classMame.substring(lastDot + 1);
        String builderClassName = classMame + "Builder";
        String builderSimpleClassName = builderClassName.substring(lastDot + 1);
        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(';');
                out.println();
            }
            out.print("public class ");
            out.print(builderSimpleClassName);
            out.println(" {");
            out.println();
            out.print("    private ");
            out.print(simpleClassName);
            out.print(" object = new ");
            out.print(simpleClassName);
            out.println("();");
            out.println();
            out.print("    public ");
            out.print(simpleClassName);
            out.println(" build() {");
            out.println("        return object;");
            out.println("    }");
            out.println();
            settersMap.forEach((methodName, argumentType) -> {
                out.print("    public ");
                out.print(builderSimpleClassName);
                out.print(" ");
                out.print(methodName);
                out.print("(");
                out.print(argumentType);
                out.println(" value) {");
                out.print("        object.");
                out.print(methodName);
                out.println("(value);");
                out.println("        return this;");
                out.println("    }");
                out.println();
            });
            out.println('}');
        }
    }
}
