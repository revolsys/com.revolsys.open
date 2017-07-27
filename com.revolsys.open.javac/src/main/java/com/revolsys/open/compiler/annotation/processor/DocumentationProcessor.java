package com.revolsys.open.compiler.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.revolsys.open.compiler.annotation.Documentation;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

@SupportedAnnotationTypes({
  "com.revolsys.open.compiler.annotation.Documentation"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class DocumentationProcessor extends AbstractProcessor {

  private JavacElements elementUtils;

  private TreeMaker maker;

  private Names names;

  private JCModifiers getModifiers(final Element element) {
    final JCTree treeElement = this.elementUtils.getTree(element);
    if (treeElement instanceof JCVariableDecl) {
      final JCVariableDecl modSrc = (JCVariableDecl)treeElement;
      return modSrc.mods;
    } else if (treeElement instanceof JCClassDecl) {
      final JCClassDecl modSrc = (JCClassDecl)treeElement;
      return modSrc.mods;
    } else if (treeElement instanceof JCMethodDecl) {
      final JCMethodDecl modSrc = (JCMethodDecl)treeElement;
      return modSrc.mods;
    } else {
      throw new IllegalArgumentException("Not supported:" + treeElement.getKind());
    }
  }

  @Override
  public void init(final ProcessingEnvironment environment) {
    super.init(environment);
    if (environment instanceof JavacProcessingEnvironment) {
      final JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment)environment;
      this.elementUtils = javacProcessingEnv.getElementUtils();
      final Context context = javacProcessingEnv.getContext();
      this.maker = TreeMaker.instance(context);
      this.names = Names.instance(context);
    }
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
    final RoundEnvironment roundEnv) {
    if (this.elementUtils != null) {
      final Set<? extends Element> elements = roundEnv
        .getElementsAnnotatedWith(Documentation.class);
      for (final Element element : elements) {
        String docComment = this.elementUtils.getDocComment(element);
        if (docComment != null) {
          docComment = docComment.trim();
          if (docComment.length() > 0) {
            final JCModifiers modifiers = getModifiers(element);
            for (final JCAnnotation annotation : modifiers.annotations) {
              if (annotation.type.toString().equals(Documentation.class.getName())) {
                boolean hasValue = false;
                final JCLiteral commentLiteral = this.maker.Literal(docComment);

                for (final JCExpression arg : annotation.args) {
                  if (arg instanceof JCAssign) {
                    final JCAssign assign = (JCAssign)arg;

                    final JCExpression lhs = assign.lhs;
                    if (lhs instanceof JCIdent) {
                      final JCIdent ident = (JCIdent)lhs;
                      if (ident.name.contentEquals("value")) {
                        assign.rhs = commentLiteral;
                        hasValue = true;
                      }
                    }
                  }
                }

                if (!hasValue) {
                  final ClassSymbol classSymbol = (ClassSymbol)((JCIdent)annotation.annotationType).sym;
                  MethodSymbol valueMethod = null;
                  for (final Symbol symbol : classSymbol.members_field
                    .getElementsByName(this.names.value)) {
                    valueMethod = (MethodSymbol)symbol;
                  }
                  final JCAssign valueArg = this.maker.Assign(this.maker.Ident(valueMethod),
                    commentLiteral);

                  annotation.args = annotation.args.append(valueArg);
                }
              }
            }
          }
        }
      }
    }

    return true;
  }
}
