import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import com.sun.source.util.Trees;

@SupportedAnnotationTypes("HelloWorld")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HelloWorldProcessor extends AbstractProcessor {

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Hello Worlds!");
    }
    return true;
  }
}
