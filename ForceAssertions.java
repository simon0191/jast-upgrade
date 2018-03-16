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

import com.sun.source.util.Trees;
import com.sun.source.util.DocTrees;
import com.sun.source.doctree.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import com.sun.tools.javac.tree.JCTree;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;

import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeIntersection;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCTypeUnion;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.util.List;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ForceAssertions extends AbstractProcessor {

  private int tally;
  private int annotated;
  private Trees trees;
  private DocTrees docTrees;
  private TreeMaker make;
  private Names names;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    trees = Trees.instance(env);
    docTrees = DocTrees.instance(env);
    Context context = ((JavacProcessingEnvironment)env).getContext();
    make = TreeMaker.instance(context);
    names = Names.instance(context);
    tally = 0;
    annotated = 0;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HelloWorld.class);
      annotated += annotatedElements.size();
      for (Element each : annotatedElements) {
        if (each.getKind() == ElementKind.METHOD) {
          DocCommentTree docTree = docTrees.getDocCommentTree(trees.getPath(each));
          String str = docTree.getFirstSentence().toString();
          if(docTree != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Comment found: " + str);
          }
          JCTree tree = (JCTree) trees.getTree(each);
          TreeTranslator visitor = new Inliner(str);
          tree.accept(visitor);
        }
      }
    } else {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, tally + " assertions inlined.");
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
    public void visitMethodDef(JCMethodDecl var1) {
      var1.mods = (JCModifiers)this.translate((JCTree)var1.mods);
      var1.restype = (JCExpression)this.translate((JCTree)var1.restype);
      var1.typarams = this.translateTypeParams(var1.typarams);
      var1.recvparam = (JCVariableDecl)this.translate((JCTree)var1.recvparam);
      var1.params = this.translateVarDefs(var1.params);
      var1.thrown = this.translate(var1.thrown);
      var1.body = make.Block(0, List.of(make.Return(make.Literal(str))));
      //var1.body = (JCBlock)this.translate((JCTree)make.Return(make.Literal(str)).getExpression());
      //var1.body = (JCBlock)make.Return(make.Literal(str));
      this.result = var1;
    }
    @Override
    public void visitAssert(JCAssert tree) {
      super.visitAssert(tree);
      JCStatement newNode = makeIfThrowException(tree);
      result = newNode;
      tally++;
    }

    private JCStatement makeIfThrowException(JCAssert node) {
      // make: if (!(condition) throw new AssertionError(node.detail);
      List<JCExpression> args = node.getDetail() == null ? List.<JCExpression>nil() : List.of(node.detail);
      JCExpression expr = make.NewClass(null, null, make.Ident(names.fromString("AssertionError")), args, null);
      return make.If(make.Unary(JCTree.Tag.NOT, node.cond), make.Throw(expr), null);
    }

  }

}