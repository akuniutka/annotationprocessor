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

@SupportedAnnotationTypes("dev.akuniutka.annotationprocessor.BuilderField")
@SupportedSourceVersion(SourceVersion.RELEASE_0)
public class BuilderFieldProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BuilderField.class);
        elements.stream().collect(
                Builder::new,
                Builder::addFieldFromElement,
                Builder::merge
        ).getClasses().forEach((Class c) -> {
            try {
                JavaFileObject file = processingEnv.getFiler().createSourceFile(c.getQualifiedName());
                try (PrintWriter out = new PrintWriter(file.openWriter())) {
                    out.print(c);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
        return true;
    }
}
