package dev.akuniutka.annotationprocessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("dev.akuniutka.annotationprocessor.BuilderField")
@SupportedSourceVersion(SourceVersion.RELEASE_0)
public class BuildProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BuilderField.class);
        elements.stream().collect(Collectors.groupingBy(
                element -> ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString(),
                Collectors.mapping(
                        element -> new Field(element.getSimpleName().toString(), element.asType().toString()),
                        Collectors.toList()
                )
        )).entrySet().stream().map(
                entry -> new ClassBuilder(entry.getKey(), entry.getValue())
        ).forEach(builder -> {
            try {
                JavaFileObject file = processingEnv.getFiler().createSourceFile(builder.getQualifiedName());
                try (PrintWriter out = new PrintWriter(file.openWriter())) {
                    builder.build(out);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
        return true;
    }
}
