package pw.smn.jastupgrade.processors;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import pw.smn.jastupgrade.annotations.StringFromComment;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class StringFromCommentProcessor extends AbstractProcessor {
  private int annotated;
  private Trees trees;
  private DocTrees docTrees;
  private TreeMaker make;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    trees = Trees.instance(env);
    docTrees = DocTrees.instance(env);
    Context context = ((JavacProcessingEnvironment)env).getContext();
    make = TreeMaker.instance(context);
    annotated = 0;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(StringFromComment.class);
      for (Element each : annotatedElements) {
        if (each.getKind() == ElementKind.METHOD) {
          annotated ++;
          DocCommentTree docTree = docTrees.getDocCommentTree(trees.getPath(each));
          String str = "";
          if(docTree != null) {
            str = docTree.getFirstSentence().toString();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Comment found: " + str);
          } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Comment not found in annotated method");
          }
          JCTree tree = (JCTree) trees.getTree(each);
          TreeTranslator visitor = new Inliner(str);
          tree.accept(visitor);
        }
      }
    } else {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, annotated + " annotated elements.");
    }
    return false;
  }

  private class Inliner extends TreeTranslator {
    private String str;
    Inliner(String str) {
      this.str = str;
    }

    @Override
    public void visitMethodDef(JCMethodDecl methodDef) {
      super.visitMethodDef(methodDef);
      methodDef.body = make.Block(0, List.of(make.Return(make.Literal(str))));
      this.result = methodDef;
    }

  }

}