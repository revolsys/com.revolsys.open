package com.revolsys.doclet11.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import com.revolsys.doclet11.BaseDoclet;
import com.revolsys.doclet11.option.ConsumerOption;
import com.revolsys.doclet11.option.FunctionOption;
import com.revolsys.io.FileUtil;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.sun.source.doctree.DocTree;

import jdk.javadoc.doclet.DocletEnvironment;

public class JavaDoclet extends BaseDoclet {
  private String docId;

  private String header;

  private String footer;

  public JavaDoclet() {
    super("Java");
    this.options.add(new ConsumerOption("docid", "Document ID", "name", this::setDocId));
    this.options.add(new FunctionOption("htmlfooter", "HTML Footer File", "file", this::setFooter));
    this.options.add(new FunctionOption("htmlheader", "HTML Header File", "file", this::setHeader));
  }

  public void bodyContent() {
    this.writer.element(HtmlElem.H1, this.docTitle);
    // description(null, this.environment);
    documentation();
  }

  @Override
  public void documentation() {
    this.writer.startTag(HtmlElem.DIV);
    for (final PackageElement PackageElement : getPackages()) {
      documentationPackage(PackageElement);
    }

    this.writer.endTagLn(HtmlElem.DIV);
  }

  public void documentationClass(final TypeElement element) {
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "javaClass");
    final Name name = element.getSimpleName();

    title(getClassId(element), name);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "content");
    description(element, element);

    final List<ExecutableElement> constructors = getConstructors(element);
    if (constructors.size() > 0) {
      title(HtmlElem.H3, "Constructors");
      for (final ExecutableElement method : constructors) {
        documentationMethod(method);
      }
    }

    final List<ExecutableElement> methods = getMethods(element);
    if (methods.size() > 0) {
      title(HtmlElem.H3, "Methods");
      for (final ExecutableElement method : methods) {
        documentationMethod(method);
      }
    }
    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public void documentationMethod(final ExecutableElement member) {
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "javaMethod");

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "title");
    methodSignature(member);
    this.writer.endTagLn(HtmlElem.DIV);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "content");
    description((TypeElement)member.getEnclosingElement(), member);

    parameters(member);

    if (member instanceof ExecutableElement) {
      final ExecutableElement method = member;
      documentationReturn(method);
    }

    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public void documentationPackage(final PackageElement packageElement) {
    final Name name = packageElement.getSimpleName();
    this.writer.startTag(HtmlElem.A);
    this.writer.attribute(HtmlAttr.NAME, name);
    this.writer.text("");
    this.writer.endTagLn(HtmlElem.A);
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "javaPackage");

    title(name, name);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "content");
    description(null, packageElement);
    final Map<Name, TypeElement> classes = new TreeMap<>();
    for (final TypeElement TypeElement : getClasses(packageElement)) {
      classes.put(TypeElement.getSimpleName(), TypeElement);
    }
    for (final TypeElement TypeElement : classes.values()) {
      documentationClass(TypeElement);
    }
    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public String getAnchor(final ExecutableElement member) {
    final StringBuilder anchor = new StringBuilder();
    final TypeElement TypeElement = (TypeElement)member.getEnclosingElement();
    final Name className = qualifiedName(TypeElement);
    anchor.append(className);
    anchor.append(".");
    anchor.append(member.getSimpleName());
    anchor.append("(");
    boolean first = true;
    for (final VariableElement parameter : member.getParameters()) {
      if (first) {
        first = false;
      } else {
        anchor.append(",");
      }
      final TypeMirror type = parameter.asType();
      String typeName = getQualifiedName(type).toString();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      anchor.append(typeName);
      anchor.append(getDimension(type));
    }
    anchor.append(")");
    return anchor.toString();
  }

  public void methodSignature(final ExecutableElement member) {
    this.writer.startTag(HtmlElem.A);
    final String anchor = getAnchor(member);
    this.writer.attribute(HtmlAttr.NAME, anchor);
    if (member instanceof ExecutableElement) {
      this.writer.startTag(HtmlElem.CODE);
      final ExecutableElement method = member;
      final TypeMirror returnType = method.getReturnType();
      typeName(returnType);
      this.writer.text(" ");
      this.writer.endTagLn(HtmlElem.CODE);
    }
    if (isStatic(member)) {
      this.writer.startTag(HtmlElem.I);
    }
    this.writer.text(member.getSimpleName());
    if (isStatic(member)) {
      this.writer.endTag(HtmlElem.I);
    }
    this.writer.startTag(HtmlElem.CODE);
    this.writer.text("(");
    boolean first = true;
    for (final VariableElement parameter : member.getParameters()) {
      if (first) {
        first = false;
      } else {
        this.writer.text(", ");
      }

      typeName(parameter.asType());
      this.writer.text(" ");
      this.writer.text(parameter.getSimpleName());
    }
    this.writer.text(")");
    this.writer.endTagLn(HtmlElem.CODE);
    this.writer.endTagLn(HtmlElem.A);
  }

  public void parameters(final ExecutableElement method) {
    final List<VariableElement> parameters = new ArrayList<>();
    for (final VariableElement parameter : method.getParameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final TypeElement getEnclosingElement = (TypeElement)method.getEnclosingElement();
      final Map<String, DocTree[]> descriptions = getVariableElementDescriptions(method);

      title(HtmlElem.H3, "VariableElements");

      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "simpleDataTable parameters");
      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "data");
      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "VariableElement");
      this.writer.element(HtmlElem.TH, "Type");
      this.writer.startTag(HtmlElem.TH);
      this.writer.attribute(HtmlAttr.CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlElem.TH);
      this.writer.endTagLn(HtmlElem.TR);
      this.writer.endTagLn(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final VariableElement parameter : parameters) {
        this.writer.startTag(HtmlElem.TR);
        final Name name = parameter.getSimpleName();

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "name");
        this.writer.text(parameter.getSimpleName());
        this.writer.endTagLn(HtmlElem.TD);

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "type");
        typeNameLink(parameter.asType());
        this.writer.endTagLn(HtmlElem.TD);

        // descriptionTd(getEnclosingElement, descriptions, name);
        this.writer.endTagLn(HtmlElem.TR);
      }
      this.writer.endTagLn(HtmlElem.TBODY);

      this.writer.endTagLn(HtmlElem.TABLE);
      this.writer.endTagLn(HtmlElem.DIV);
    }
  }

  @Override
  protected void preRun() {
    super.preRun();
    FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.css"),
      new File(this.destDir, "javadoc.css"));
    FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.js"),
      new File(this.destDir, "javadoc.js"));
  }

  @Override
  public boolean run(final DocletEnvironment root) {
    preRun();
    try {
      if (this.header == null) {
        this.writer.startDocument("UTF-8", "1.0");
        this.writer.docType("html", null);
        this.writer.startTag(HtmlElem.HTML);
        this.writer.attribute(HtmlAttr.LANG, "en");

        headOld(this.docTitle);
        this.writer.startTag(HtmlElem.BODY);
      } else {
        this.header = this.header.replaceAll("\\$\\{docTitle\\}", this.docTitle);
        this.header = this.header.replaceAll("\\$\\{docId\\}", this.docId);
        this.writer.write(this.header);
      }

      bodyContent();

      if (this.footer == null) {
        this.writer.endTagLn(HtmlElem.BODY);

        this.writer.endTagLn(HtmlElem.HTML);
      } else {
        this.footer = this.footer.replaceAll("\\$\\{docTitle\\}", this.docTitle);
        this.footer = this.footer.replaceAll("\\$\\{docId\\}", this.docId);
        this.writer.write(this.footer);
      }
      this.writer.endDocument();
    } finally {
      if (this.writer != null) {
        this.writer.close();
      }
    }
    return true;
  }

  public void setDocId(final String docId) {
    this.docId = docId;
  }

  public boolean setFooter(final String footer) {
    if (new File(footer).exists()) {
      this.footer = FileUtil.getFileAsString(footer);
      return true;
    } else {
      this.reporter.print(Kind.ERROR, "Footer file does not exist " + footer);
      return false;
    }
  }

  public boolean setHeader(final String header) {
    this.header = header;
    if (new File(header).exists()) {
      this.header = FileUtil.getFileAsString(header);
      return true;
    } else {
      this.reporter.print(Kind.ERROR, "Header file does not exist " + header);
      return false;
    }
  }

}
