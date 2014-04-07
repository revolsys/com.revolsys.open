package com.revolsys.doclet.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.doclet.DocletUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class ClientDoclet {
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d") || optionName.equals("-doctitle")
      || optionName.equals("-docid") || optionName.equals("-htmlfooter")
      || optionName.equals("-htmlheader") || optionName.equals("-packagesOpen")) {
      return 2;
    }
    return -1;
  }

  public static boolean start(final RootDoc root) {
    new ClientDoclet(root).start();
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
          docerrorreporter.printError("Destination not a directory"
            + file.getPath());
          return false;
        } else if (!file.canWrite()) {
          docerrorreporter.printError("Destination directory not writable "
            + file.getPath());
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
      } else if (argName.equals("-packagesOpen")) {
        if (!"true".equalsIgnoreCase(option[1])
          && !"false".equalsIgnoreCase(option[1])) {
          docerrorreporter.printError("PackagesOpen must be true or false not "
            + option[1]);
          return false;
        }
      }
    }

    return flag;
  }

  private String docTitle;

  private String docId;

  private final RootDoc root;

  private XmlWriter writer;

  private String destDir = ".";

  private String header;

  private String footer;

  private boolean packagesOpen = true;

  public ClientDoclet(final RootDoc root) {
    this.root = root;
  }

  public void addResponseStatusDescription(
    final Map<String, List<String>> responseCodes, final String code,
    final String description) {
    List<String> descriptions = responseCodes.get(code);
    if (descriptions == null) {
      descriptions = new ArrayList<String>();
      responseCodes.put(code, descriptions);
    }
    descriptions.add(description);
  }

  public void bodyContent() {
    writer.element(HtmlUtil.H1, docTitle);
    DocletUtil.description(writer, null, root);
    documentation();
  }

  public void documentation() {
    writer.startTag(HtmlUtil.DIV);
    for (final PackageDoc packageDoc : root.specifiedPackages()) {
      documentationPackage(packageDoc);
    }

    writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationAnnotation(final AnnotationTypeDoc annotationDoc) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = annotationDoc.name();

    final String id = DocletUtil.qualifiedName(annotationDoc);
    HtmlUtil.elementWithId(writer, HtmlUtil.H2, id, name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, annotationDoc, annotationDoc);

    final AnnotationTypeElementDoc[] elements = annotationDoc.elements();
    if (elements.length > 0) {
      DocletUtil.title(writer, "Annotation Elements");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Column");
      writer.element(HtmlUtil.TH, "Type");
      writer.element(HtmlUtil.TH, "Default");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTagLn(HtmlUtil.TR);
      writer.endTagLn(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final AnnotationTypeElementDoc element : elements) {
        writer.startTag(HtmlUtil.TR);
        final String elementName = element.name();

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        DocletUtil.anchor(writer, id + "." + elementName, elementName);
        writer.endTagLn(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "type");
        DocletUtil.typeNameLink(writer, element.returnType());
        writer.endTagLn(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "default");
        final AnnotationValue defaultValue = element.defaultValue();
        if (defaultValue == null) {
          writer.text("-");
        } else {
          writer.text(defaultValue);
        }
        writer.endTagLn(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "description");
        DocletUtil.description(writer, null, element);
        writer.endTagLn(HtmlUtil.TD);
        writer.endTagLn(HtmlUtil.TR);
      }
      writer.endTagLn(HtmlUtil.TBODY);

      writer.endTagLn(HtmlUtil.TABLE);
      writer.endTagLn(HtmlUtil.DIV);

    }
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationAnnotations(final PackageDoc packageDoc) {
    final Map<String, AnnotationTypeDoc> annotations = new TreeMap<String, AnnotationTypeDoc>();
    for (final AnnotationTypeDoc annotationDoc : packageDoc.annotationTypes()) {
      annotations.put(annotationDoc.name(), annotationDoc);
    }
    if (!annotations.isEmpty()) {
      DocletUtil.title(writer, "Annotations");
      for (final AnnotationTypeDoc annotationDoc : annotations.values()) {
        documentationAnnotation(annotationDoc);
      }
    }
  }

  public void documentationClass(final ClassDoc classDoc) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = classDoc.name();

    final String id = DocletUtil.qualifiedName(classDoc);
    HtmlUtil.elementWithId(writer, HtmlUtil.H2, id, name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, classDoc, classDoc);

    final ConstructorDoc[] constructors = classDoc.constructors();
    if (constructors.length > 0) {
      DocletUtil.title(writer, "Constructors");
      for (final ConstructorDoc method : constructors) {
        documentationMethod(method);
      }
    }

    final MethodDoc[] methods = classDoc.methods();
    if (methods.length > 0) {
      DocletUtil.title(writer, "Methods");
      for (final MethodDoc method : methods) {
        documentationMethod(method);
      }
    }
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationClasses(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> classes = new TreeMap<String, ClassDoc>();
    for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
      classes.put(classDoc.name(), classDoc);
    }
    if (!classes.isEmpty()) {
      DocletUtil.title(writer, "Classes");
      for (final ClassDoc classDoc : classes.values()) {
        documentationClass(classDoc);
      }
    }
  }

  public void documentationEnum(final ClassDoc enumDoc) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = enumDoc.name();

    final String id = DocletUtil.qualifiedName(enumDoc);
    HtmlUtil.elementWithId(writer, HtmlUtil.H2, id, name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, enumDoc, enumDoc);

    final FieldDoc[] elements = enumDoc.enumConstants();
    if (elements.length > 0) {
      DocletUtil.title(writer, "Enum Constants");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Constant");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTagLn(HtmlUtil.TR);
      writer.endTagLn(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final FieldDoc element : elements) {
        writer.startTag(HtmlUtil.TR);
        final String elementName = element.name();

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "constant");
        writer.text(elementName);
        writer.endTagLn(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "description");
        DocletUtil.description(writer, null, element);
        writer.endTagLn(HtmlUtil.TD);
        writer.endTagLn(HtmlUtil.TR);
      }
      writer.endTagLn(HtmlUtil.TBODY);

      writer.endTagLn(HtmlUtil.TABLE);
      writer.endTagLn(HtmlUtil.DIV);

    }
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationEnums(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> enums = new TreeMap<String, ClassDoc>();
    for (final ClassDoc enumDoc : packageDoc.enums()) {
      enums.put(enumDoc.name(), enumDoc);
    }
    if (!enums.isEmpty()) {
      DocletUtil.title(writer, "Enums");
      for (final ClassDoc enumDoc : enums.values()) {
        documentationEnum(enumDoc);
      }
    }
  }

  public void documentationInterfaces(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> interfaces = new TreeMap<String, ClassDoc>();
    for (final ClassDoc classDoc : packageDoc.interfaces()) {
      interfaces.put(classDoc.name(), classDoc);
    }
    if (!interfaces.isEmpty()) {
      DocletUtil.title(writer, "Interfaces");
      for (final ClassDoc classDoc : interfaces.values()) {
        documentationClass(classDoc);
      }
    }
  }

  public void documentationMethod(final ExecutableMemberDoc member) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaMethod");

    writer.startTag(HtmlUtil.H3);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.attribute(HtmlUtil.ATTR_ID, getId(member));
    writer.attribute(HtmlUtil.ATTR_TITLE, member.name());
    methodSignature(member);
    writer.endTagLn(HtmlUtil.H3);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, member.containingClass(), member);

    parameters(member);

    if (member instanceof MethodDoc) {
      final MethodDoc method = (MethodDoc)member;
      DocletUtil.documentationReturn(writer, method);
    }

    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public void documentationPackage(final PackageDoc packageDoc) {
    final String name = packageDoc.name();
    writer.startTag(HtmlUtil.DIV);
    String cssClass = "javaPackage";
    if (packagesOpen) {
      cssClass += " open";
    }
    writer.attribute(HtmlUtil.ATTR_CLASS, cssClass);

    final String id = name;
    HtmlUtil.elementWithId(writer, HtmlUtil.H1, id, name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    DocletUtil.description(writer, null, packageDoc);

    documentationAnnotations(packageDoc);
    documentationEnums(packageDoc);
    documentationInterfaces(packageDoc);
    documentationClasses(packageDoc);

    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
  }

  private String getAnchor(final ExecutableMemberDoc member) {
    final StringBuffer anchor = new StringBuffer();
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

  private String getId(final ExecutableMemberDoc member) {
    final StringBuffer anchor = new StringBuffer();
    final ClassDoc classDoc = member.containingClass();
    final String className = DocletUtil.qualifiedName(classDoc);
    anchor.append(className);
    anchor.append(".");
    anchor.append(member.name());
    final Parameter[] parameters = member.parameters();
    for (final Parameter parameter : parameters) {
      anchor.append("-");
      final Type type = parameter.type();
      String typeName = type.qualifiedTypeName();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      anchor.append(typeName);
      anchor.append(type.dimension());
    }
    return anchor.toString().replaceAll("[^A-Za-z0-9\\-_.]", "_");
  }

  public void methodSignature(final ExecutableMemberDoc member) {
    writer.startTag(HtmlUtil.A);
    final String anchor = getAnchor(member);
    writer.attribute(HtmlUtil.ATTR_NAME, anchor);
    if (member instanceof MethodDoc) {
      writer.startTag(HtmlUtil.CODE);
      final MethodDoc method = (MethodDoc)member;
      final Type returnType = method.returnType();
      DocletUtil.typeName(writer, returnType);
      writer.text(" ");
      writer.endTagLn(HtmlUtil.CODE);
    }
    if (member.isStatic()) {
      writer.startTag(HtmlUtil.I);
    }
    writer.text(member.name());
    if (member.isStatic()) {
      writer.endTag(HtmlUtil.I);
    }
    writer.startTag(HtmlUtil.CODE);
    writer.text("(");
    final Parameter[] parameters = member.parameters();
    boolean first = true;
    for (final Parameter parameter : parameters) {
      if (first) {
        first = false;
      } else {
        writer.text(", ");
      }

      DocletUtil.typeName(writer, parameter.type());
      writer.text(" ");
      writer.text(parameter.name());
    }
    writer.text(")");
    writer.endTagLn(HtmlUtil.CODE);
    writer.endTagLn(HtmlUtil.A);
  }

  private void parameters(final ExecutableMemberDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      DocletUtil.title(writer, "Parameters");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Parameter");
      writer.element(HtmlUtil.TH, "Type");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTagLn(HtmlUtil.TR);
      writer.endTagLn(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final Parameter parameter : parameters) {
        writer.startTag(HtmlUtil.TR);
        final String name = parameter.name();

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        writer.text(parameter.name());
        writer.endTagLn(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.attribute(HtmlUtil.ATTR_CLASS, "type");

        final Type type = parameter.type();
        DocletUtil.typeNameLink(writer, type);
        writer.endTagLn(HtmlUtil.TD);

        DocletUtil.descriptionTd(writer, method.containingClass(),
          descriptions, name);
        writer.endTagLn(HtmlUtil.TR);
      }
      writer.endTagLn(HtmlUtil.TBODY);

      writer.endTagLn(HtmlUtil.TABLE);
      writer.endTagLn(HtmlUtil.DIV);
    }
  }

  private void setOptions(final String[][] options) {
    for (final String[] option : options) {
      final String optionName = option[0];
      if (optionName.equals("-d")) {
        destDir = option[1];

      } else if (optionName.equals("-doctitle")) {
        docTitle = option[1];
      } else if (optionName.equals("-docid")) {
        docId = option[1];
      } else if (optionName.equals("-htmlheader")) {
        header = FileUtil.getFileAsString(option[1]);
      } else if (optionName.equals("-htmlfooter")) {
        footer = FileUtil.getFileAsString(option[1]);
      } else if (optionName.equals("-packagesopen")) {
        packagesOpen = Boolean.valueOf(option[1]);
      }
    }
    try {
      final File dir = new File(destDir);
      final File indexFile = new File(dir, "index.html");
      final FileWriter out = new FileWriter(indexFile);
      writer = new XmlWriter(out, false);
      writer.setIndent(false);
      writer.setWriteNewLine(false);
      DocletUtil.copyFiles(destDir);
    } catch (final IOException e) {
      throw new IllegalArgumentException(e.fillInStackTrace().getMessage(), e);
    }
  }

  private void start() {
    try {
      setOptions(root.options());

      if (header == null) {
        writer.startDocument("UTF-8", "1.0");
        writer.docType("html", null);
        writer.startTag(HtmlUtil.HTML);
        writer.attribute(HtmlUtil.ATTR_LANG, "en");

        DocletUtil.head(writer, docTitle);
        writer.startTag(HtmlUtil.BODY);
      } else {
        header = header.replaceAll("\\$\\{docTitle\\}", docTitle);
        header = header.replaceAll("\\$\\{docId\\}", docId);
        writer.write(header);
      }

      bodyContent();

      if (footer == null) {
        writer.endTagLn(HtmlUtil.BODY);

        writer.endTagLn(HtmlUtil.HTML);
      } else {
        footer = footer.replaceAll("\\$\\{docTitle\\}", docTitle);
        footer = footer.replaceAll("\\$\\{docId\\}", docId);
        writer.write(footer);
      }
      writer.endDocument();
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

}
