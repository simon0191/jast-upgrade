package pw.smn.jastupgrade.processors;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DebugLabelRemoverProcessor extends AbstractProcessor {
  private Trees trees;
  private TreeMaker make;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    trees = Trees.instance(env);
    Context context = ((JavacProcessingEnvironment) env).getContext();
    make = TreeMaker.instance(context);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      Set<? extends Element> annotatedElements =
          roundEnv.getRootElements();

      for (Element each : annotatedElements) {
          JCTree tree = (JCTree) trees.getTree(each);
          TreeTranslator visitor = new Inliner();
          tree.accept(visitor);
      }
    }

    return false;
  }

  private class Inliner extends TreeTranslator {  

    @Override
    public void visitLabelled(JCLabeledStatement labelled) {
      if(labelled.getLabel().toString().equals("debug")) {
        processingEnv
          .getMessager()
          .printMessage(Diagnostic.Kind.NOTE, "debug label faound");
        result = make.Skip();
      } else {
        super.visitLabelled(labelled);
      }
    }
  }
}
