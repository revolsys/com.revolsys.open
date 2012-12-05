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
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

public class ClientDoclet {

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d") || optionName.equals("-doctitle")
      || optionName.equals("-htmlfooter") || optionName.equals("-htmlheader")) {
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
      }
    }

    return flag;
  }

  private String docTitle;

  private final RootDoc root;

  private XmlWriter writer;

  private String destDir = ".";

  private String header;

  private String footer;

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

    documentation();
  }

  public void description(final Map<String, String> descriptions,
    final String name) {
    writer.startTag(HtmlUtil.TD);
    final String description = descriptions.get(name);
    if (description == null) {
      writer.write("-");
    } else {
      writer.write(description);
    }
    writer.endTag(HtmlUtil.TD);
  }

  public void documentation() {
    writer.startTag(HtmlUtil.DIV);
    for (final PackageDoc packageDoc : root.specifiedPackages()) {
      documentationPackage(packageDoc);
    }

    writer.endTag(HtmlUtil.DIV);
  }

  public void documentationClass(final ClassDoc classDoc) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass");
    final String name = classDoc.name();

    title(DocletUtil.qualifiedName(classDoc), name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    writer.write(classDoc.commentText());

    final ConstructorDoc[] constructors = classDoc.constructors();
    if (constructors.length > 0) {
      title("Constructors");
      for (final ConstructorDoc method : constructors) {
        documentationMethod(classDoc, method);
      }
    }

    final MethodDoc[] methods = classDoc.methods();
    if (methods.length > 0) {
      title("Methods");
      for (final MethodDoc method : methods) {
        documentationMethod(classDoc, method);
      }
    }
    writer.endTag(HtmlUtil.DIV);
    writer.endTag(HtmlUtil.DIV);
  }

  public void documentationMethod(final ClassDoc classDoc,
    final ExecutableMemberDoc method) {
    final String methodName = method.name();

    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, DocletUtil.qualifiedName(classDoc)
      + "." + methodName);
    writer.text("");
    writer.endTag(HtmlUtil.A);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaMethod");

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    methodSignature(method);
    writer.endTag(HtmlUtil.DIV);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    writer.write(method.commentText());

    parameters(method);

    writer.endTag(HtmlUtil.DIV);
    writer.endTag(HtmlUtil.DIV);
  }

  public void documentationPackage(final PackageDoc packageDoc) {
    final String name = packageDoc.name();
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, name);
    writer.text("");
    writer.endTag(HtmlUtil.A);
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaPackage");

    title(name, name);

    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "content");
    writer.write(packageDoc.commentText());
    final Map<String, ClassDoc> classes = new TreeMap<String, ClassDoc>();
    for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
      classes.put(classDoc.name(), classDoc);
    }
    for (final ClassDoc classDoc : classes.values()) {
      documentationClass(classDoc);
    }
    writer.endTag(HtmlUtil.DIV);
    writer.endTag(HtmlUtil.DIV);

  }

  public void head() {
    writer.startTag(HtmlUtil.HEAD);
    writer.element(HtmlUtil.TITLE, docTitle);
    HtmlUtil.serializeCss(
      writer,
      "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/css/jquery.dataTables_themeroller.css");
    HtmlUtil.serializeCss(
      writer,
      "http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/themes/cupertino/jquery-ui.css");
    HtmlUtil.serializeCss(writer, "javadoc.css");
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js");
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/jquery-ui.min.js");
    HtmlUtil.serializeScriptLink(
      writer,
      "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/jquery.dataTables.min.js");
    HtmlUtil.serializeScriptLink(writer, "javadoc.js");
    writer.endTag(HtmlUtil.HEAD);
  }

  public void methodSignature(final ExecutableMemberDoc member) {
    if (member instanceof MethodDoc) {
      writer.startTag(HtmlUtil.CODE);
      final MethodDoc method = (MethodDoc)member;
      final Type returnType = method.returnType();
      writer.text(returnType.typeName());
      writer.text(" ");
      writer.endTag(HtmlUtil.CODE);
    }
    writer.text(member.name());
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
      writer.text(parameter.typeName());
      writer.text(" ");
      writer.text(parameter.name());
    }
    writer.text(")");
    writer.endTag(HtmlUtil.CODE);
  }

  private void parameters(final ExecutableMemberDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final Map<String, String> descriptions = DocletUtil.getParameterDescriptions(method);

      title("Parameters");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_STYLE, "width:auto;margin-left:0px");
      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Parameter");
      writer.element(HtmlUtil.TH, "Type");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTag(HtmlUtil.TR);
      writer.endTag(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final Parameter parameter : parameters) {
        writer.startTag(HtmlUtil.TR);
        final String name = parameter.name();

        writer.startTag(HtmlUtil.TD);
        writer.startTag(HtmlUtil.CODE);
        writer.text(parameter.name());
        writer.endTag(HtmlUtil.CODE);
        writer.endTag(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.startTag(HtmlUtil.CODE);
        typeName(parameter.type());
        writer.endTag(HtmlUtil.CODE);
        writer.endTag(HtmlUtil.TD);

        description(descriptions, name);
        writer.endTag(HtmlUtil.TR);
      }
      writer.endTag(HtmlUtil.TBODY);

      writer.endTag(HtmlUtil.TABLE);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  private void setOptions(final String[][] options) {
    for (final String[] option : options) {
      final String optionName = option[0];
      if (optionName.equals("-d")) {
        destDir = option[1];

      } else if (optionName.equals("-doctitle")) {
        docTitle = option[1];
      } else if (optionName.equals("-htmlheader")) {
        header = FileUtil.getFileAsString(option[1]);
      } else if (optionName.equals("-htmlfooter")) {
        footer = FileUtil.getFileAsString(option[1]);
      }
    }
    try {
      final File dir = new File(destDir);
      final File indexFile = new File(dir, "index.html");
      final FileWriter out = new FileWriter(indexFile);
      writer = new XmlWriter(out, false);
      FileUtil.copy(
        getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.css"),
        new File(destDir, "javadoc.css"));
      FileUtil.copy(
        getClass().getResourceAsStream("/com/revolsys/doclet/javadoc.js"),
        new File(destDir, "javadoc.js"));
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

        head();
        writer.startTag(HtmlUtil.BODY);
      } else {
        writer.write(header);
      }

      bodyContent();

      if (footer == null) {
        writer.endTag(HtmlUtil.BODY);

        writer.endTag(HtmlUtil.HTML);
      } else {
        writer.write(footer);
      }
      writer.endDocument();
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public void title(final String title) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.text(title);
    writer.endTag(HtmlUtil.DIV);
  }

  public void title(final String name, final String title) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, name);
    writer.text(title);
    writer.endTag(HtmlUtil.A);
    writer.endTag(HtmlUtil.DIV);
  }

  public void typeName(final Type returnType) {
    final String qualifiedTypeName = returnType.qualifiedTypeName();
    if (qualifiedTypeName.startsWith("java.")) {
      final String url = "http://docs.oracle.com/javase/6/docs/api/"
        + qualifiedTypeName.replaceAll("\\.", "/") + ".html?is-external=true";
      HtmlUtil.serializeA(writer, "", url, returnType.typeName());
    } else {
      writer.text(returnType);
    }
  }

}
