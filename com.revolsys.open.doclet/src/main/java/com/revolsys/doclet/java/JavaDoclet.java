package com.revolsys.doclet.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.doclet.BaseDoclet;
import com.revolsys.doclet.DocletUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

public class JavaDoclet extends BaseDoclet {
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static int optionLength(String optionName) {
    final int optionLength = DocletUtil.optionLength(optionName);
    if (optionLength == 0) {
      optionName = optionName.toLowerCase();
      if (optionName.equals("-docid") || optionName.equals("-htmlfooter")
        || optionName.equals("-htmlheader")) {
        return 2;
      }
    }
    return optionLength;
  }

  public static boolean start(final RootDoc root) {
    new JavaDoclet(root).start();
    return true;
  }

  public static boolean validOptions(final String options[][],
    final DocErrorReporter docerrorreporter) {
    final boolean flag = true;
    for (final String[] option : options) {
      final String argName = option[0].toLowerCase();
      if (argName.equals("-d")) {
        final String destDir = option[1];
        final File file = new File(destDir);
        if (!file.exists()) {
          docerrorreporter.printNotice("Create directory" + destDir);
          file.mkdirs();
        }
        if (!file.isDirectory()) {
          docerrorreporter.printError("Destination not a directory" + file.getPath());
          return false;
        } else if (!file.canWrite()) {
          docerrorreporter.printError("Destination directory not writable " + file.getPath());
          return false;
        }
      } else if (argName.equals("-htmlheader")) {
        if (!new File(option[1]).exists()) {
          docerrorreporter.printError("Header file does not exist" + option[1]);
          return false;
        }
      } else if (argName.equals("-htmlfooter")) {
        if (!new File(option[1]).exists()) {
          docerrorreporter.printError("Footer file does not exist" + option[1]);
          return false;
        }
      }
    }

    return flag;
  }

  private String docId;

  private String header;

  private String footer;

  public JavaDoclet(final RootDoc root) {
    super(root);
  }

  public void bodyContent() {
    this.writer.element(HtmlElem.H1, this.docTitle);
    DocletUtil.description(this.writer, null, this.root);
    documentation();
  }

  @Override
  public void documentation() {
    this.writer.startTag(HtmlElem.DIV);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      documentationPackage(packageDoc);
    }

    this.writer.endTagLn(HtmlElem.DIV);
  }

  public void documentationClass(final ClassDoc classDoc) {
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "javaClass");
    final String name = classDoc.name();

    DocletUtil.title(this.writer, getClassId(classDoc), name);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "content");
    DocletUtil.description(this.writer, classDoc, classDoc);

    final ConstructorDoc[] constructors = classDoc.constructors();
    if (constructors.length > 0) {
      DocletUtil.title(this.writer, HtmlElem.H3, "Constructors");
      for (final ConstructorDoc method : constructors) {
        documentationMethod(method);
      }
    }

    final MethodDoc[] methods = classDoc.methods();
    if (methods.length > 0) {
      DocletUtil.title(this.writer, HtmlElem.H3, "Methods");
      for (final MethodDoc method : methods) {
        documentationMethod(method);
      }
    }
    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public void documentationMethod(final ExecutableMemberDoc member) {
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "javaMethod");

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "title");
    methodSignature(member);
    this.writer.endTagLn(HtmlElem.DIV);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "content");
    DocletUtil.description(this.writer, member.containingClass(), member);

    parameters(member);

    if (member instanceof MethodDoc) {
      final MethodDoc method = (MethodDoc)member;
      DocletUtil.documentationReturn(this.writer, method);
    }

    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public void documentationPackage(final PackageDoc packageDoc) {
    final String name = packageDoc.name();
    this.writer.startTag(HtmlElem.A);
    this.writer.attribute(HtmlAttr.NAME, name);
    this.writer.text("");
    this.writer.endTagLn(HtmlElem.A);
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "javaPackage");

    DocletUtil.title(this.writer, name, name);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "content");
    DocletUtil.description(this.writer, null, packageDoc);
    final Map<String, ClassDoc> classes = new TreeMap<>();
    for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
      classes.put(classDoc.name(), classDoc);
    }
    for (final ClassDoc classDoc : classes.values()) {
      documentationClass(classDoc);
    }
    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public String getAnchor(final ExecutableMemberDoc member) {
    final StringBuilder anchor = new StringBuilder();
    final ClassDoc classDoc = member.containingClass();
    final String className = DocletUtil.qualifiedName(classDoc);
    anchor.append(className);
    anchor.append(".");
    anchor.append(member.name());
    anchor.append("(");
    final Parameter[] parameters = member.parameters();
    boolean first = true;
    for (final Parameter parameter : parameters) {
      if (first) {
        first = false;
      } else {
        anchor.append(",");
      }
      final Type type = parameter.type();
      String typeName = type.qualifiedTypeName();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      anchor.append(typeName);
      anchor.append(type.dimension());
    }
    anchor.append(")");
    return anchor.toString();
  }

  public void methodSignature(final ExecutableMemberDoc member) {
    this.writer.startTag(HtmlElem.A);
    final String anchor = getAnchor(member);
    this.writer.attribute(HtmlAttr.NAME, anchor);
    if (member instanceof MethodDoc) {
      this.writer.startTag(HtmlElem.CODE);
      final MethodDoc method = (MethodDoc)member;
      final Type returnType = method.returnType();
      DocletUtil.typeName(this.writer, returnType);
      this.writer.text(" ");
      this.writer.endTagLn(HtmlElem.CODE);
    }
    if (member.isStatic()) {
      this.writer.startTag(HtmlElem.I);
    }
    this.writer.text(member.name());
    if (member.isStatic()) {
      this.writer.endTag(HtmlElem.I);
    }
    this.writer.startTag(HtmlElem.CODE);
    this.writer.text("(");
    final Parameter[] parameters = member.parameters();
    boolean first = true;
    for (final Parameter parameter : parameters) {
      if (first) {
        first = false;
      } else {
        this.writer.text(", ");
      }

      DocletUtil.typeName(this.writer, parameter.type());
      this.writer.text(" ");
      this.writer.text(parameter.name());
    }
    this.writer.text(")");
    this.writer.endTagLn(HtmlElem.CODE);
    this.writer.endTagLn(HtmlElem.A);
  }

  public void parameters(final ExecutableMemberDoc method) {
    final List<Parameter> parameters = new ArrayList<>();
    for (final Parameter parameter : method.parameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final ClassDoc containingClass = method.containingClass();
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      DocletUtil.title(this.writer, HtmlElem.H3, "Parameters");

      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "simpleDataTable parameters");
      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "data");
      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "Parameter");
      this.writer.element(HtmlElem.TH, "Type");
      this.writer.startTag(HtmlElem.TH);
      this.writer.attribute(HtmlAttr.CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlElem.TH);
      this.writer.endTagLn(HtmlElem.TR);
      this.writer.endTagLn(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final Parameter parameter : parameters) {
        this.writer.startTag(HtmlElem.TR);
        final String name = parameter.name();

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "name");
        this.writer.text(parameter.name());
        this.writer.endTagLn(HtmlElem.TD);

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "type");
        DocletUtil.typeNameLink(this.writer, parameter.type());
        this.writer.endTagLn(HtmlElem.TD);

        DocletUtil.descriptionTd(this.writer, containingClass, descriptions, name);
        this.writer.endTagLn(HtmlElem.TR);
      }
      this.writer.endTagLn(HtmlElem.TBODY);

      this.writer.endTagLn(HtmlElem.TABLE);
      this.writer.endTagLn(HtmlElem.DIV);
    }
  }

  @Override
  protected void setOptions(final String[][] options) {
    for (final String[] option : options) {
      final String optionName = option[0];
      if (optionName.equals("-docid")) {
        this.docId = option[1];
      } else if (optionName.equals("-htmlheader")) {
        this.header = FileUtil.getFileAsString(option[1]);
      } else if (optionName.equals("-htmlfooter")) {
        this.footer = FileUtil.getFileAsString(option[1]);
      }
    }
    super.setOptions(options);
    FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.css"),
      new File(this.destDir, "javadoc.css"));
    FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.js"),
      new File(this.destDir, "javadoc.js"));
  }

  @Override
  protected void start() {
    try {
      setOptions(this.root.options());

      if (this.header == null) {
        this.writer.startDocument("UTF-8", "1.0");
        this.writer.docType("html", null);
        this.writer.startTag(HtmlElem.HTML);
        this.writer.attribute(HtmlAttr.LANG, "en");

        DocletUtil.headOld(this.writer, this.docTitle);
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
  }

}
