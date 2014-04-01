package com.revolsys.doclet.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.doclet.DocletUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class RestDoclet {
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d") || optionName.equals("-doctitle")
      || optionName.equals("-docid") || optionName.equals("-htmlfooter")
      || optionName.equals("-htmlheader")) {
      return 2;
    }
    return -1;
  }

  public static boolean start(final RootDoc root) {
    new RestDoclet(root).start();
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

  private String docId;

  private String destDir = ".";

  private String docTitle;

  private String footer;

  private String header;

  private final RootDoc root;

  private XmlWriter writer;

  public RestDoclet(final RootDoc root) {
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

  public void documentation() {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "javaPackage open");

    HtmlUtil.elementWithId(writer, HtmlUtil.H1, docId, docTitle);
    DocletUtil.description(writer, null, root);
    for (final PackageDoc packageDoc : root.specifiedPackages()) {

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "content");
      final Map<String, ClassDoc> classes = new TreeMap<String, ClassDoc>();
      for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
        classes.put(classDoc.name(), classDoc);
      }
      for (final ClassDoc classDoc : classes.values()) {
        documentationClass(classDoc);
      }
    }
    writer.endTag(HtmlUtil.DIV);
    writer.endTag(HtmlUtil.DIV);
  }

  public void documentationClass(final ClassDoc classDoc) {
    if (DocletUtil.hasAnnotation(classDoc,
      "org.springframework.stereotype.Controller")) {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "javaClass open");

      final String id = DocletUtil.qualifiedName(classDoc);
      final String name = classDoc.name();
      final String title = CaseConverter.toCapitalizedWords(name);
      HtmlUtil.elementWithId(writer, HtmlUtil.H2, id, title);

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "content");
      DocletUtil.description(writer, classDoc, classDoc);
      documentationMethod(classDoc);
      writer.endTag(HtmlUtil.DIV);

      writer.endTag(HtmlUtil.DIV);
    }
  }

  public void documentationMethod(final ClassDoc classDoc) {
    for (final MethodDoc method : classDoc.methods()) {
      final AnnotationDesc requestMapping = DocletUtil.getAnnotation(method,
        "org.springframework.web.bind.annotation.RequestMapping");
      if (requestMapping != null) {
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "javaMethod");

        final String name = method.name();
        final String id = DocletUtil.qualifiedName(classDoc) + "." + name;
        final String title = CaseConverter.toCapitalizedWords(name);
        HtmlUtil.elementWithId(writer, HtmlUtil.H3, id, title);

        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "content");
        DocletUtil.description(writer, method.containingClass(), method);
        requestMethods(requestMapping);
        uriTemplates(requestMapping);
        uriTemplateParameters(method);
        parameters(method);
        responseStatus(method);
        writer.endTag(HtmlUtil.DIV);

        writer.endTag(HtmlUtil.DIV);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getElementValue(final AnnotationDesc annotation,
    final String name) {
    for (final ElementValuePair pair : annotation.elementValues()) {
      if (pair.element().name().equals(name)) {
        return (T)pair.value().value();
      }
    }
    return null;
  }

  public void head() {
    writer.startTag(HtmlUtil.HEAD);
    writer.element(HtmlUtil.TITLE, docTitle);
    HtmlUtil.serializeCss(
      writer,
      "https://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables_themeroller.css");
    HtmlUtil.serializeCss(
      writer,
      "https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/cupertino/jquery-ui.css");
    HtmlUtil.serializeCss(writer, "javadoc.css");
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js");
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js");
    HtmlUtil.serializeScriptLink(
      writer,
      "https://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js");
    HtmlUtil.serializeScriptLink(writer, "javadoc.js");
    writer.endTag(HtmlUtil.HEAD);
  }

  private void parameters(final MethodDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      final AnnotationDesc[] annotations = parameter.annotations();
      if (DocletUtil.hasAnnotation(annotations,
        "org.springframework.web.bind.annotation.RequestParam")
        || DocletUtil.hasAnnotation(annotations,
          "org.springframework.web.bind.annotation.RequestBody")) {
        parameters.add(parameter);
      }
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      writer.element(HtmlUtil.H4, "Parameters");
      writer.element(
        HtmlUtil.P,
        "The resource supports the following parameters. "
          + "For HTTP get requests these must be specified using query string parameters. "
          + "For HTTP POST requests these can be specified using query string, application/x-www-form-urlencoded parameters or multipart/form-data unless otherwise specified. "
          + "Array values [] can be specified by including the parameter multiple times in the request.");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");

      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Parameter");
      writer.element(HtmlUtil.TH, "Type");
      writer.element(HtmlUtil.TH, "Default");
      writer.element(HtmlUtil.TH, "Required");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTag(HtmlUtil.TR);
      writer.endTag(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final Parameter parameter : parameters) {
        writer.startTag(HtmlUtil.TR);
        final String name = parameter.name();
        final AnnotationDesc requestParam = DocletUtil.getAnnotation(
          parameter.annotations(),
          "org.springframework.web.bind.annotation.RequestParam");
        final AnnotationDesc requestBody = DocletUtil.getAnnotation(
          parameter.annotations(),
          "org.springframework.web.bind.annotation.RequestBody");
        String paramName = name;
        String defaultValue = "-";
        String typeName = parameter.typeName();
        typeName = typeName.replaceAll("java.util.List<([^>]+)>", "$1\\[\\]");
        typeName = typeName.replaceFirst("^java.lang.", "");
        typeName = typeName.replaceAll(
          "org.springframework.web.multipart.MultipartFile", "File");

        boolean required = true;
        if (requestParam != null) {
          final String value = getElementValue(requestParam, "value");
          if (value != null && !value.trim().equals("")) {
            paramName = value;
          }
          defaultValue = getElementValue(requestParam, "defaultValue");
          if (defaultValue == null) {
            defaultValue = "-";
          }
          required = Boolean.FALSE != (Boolean)getElementValue(requestParam,
            "required");
        }
        if (requestBody != null) {
          required = true;
          paramName = "HTTP Request body or 'body' parameter";
          typeName = "binary/character data";
        }

        writer.startTag(HtmlUtil.TD);
        writer.startTag(HtmlUtil.CODE);
        writer.text(paramName);
        writer.endTag(HtmlUtil.CODE);
        writer.endTag(HtmlUtil.TD);

        writer.startTag(HtmlUtil.TD);
        writer.startTag(HtmlUtil.CODE);
        writer.text(typeName);
        writer.endTag(HtmlUtil.CODE);
        writer.endTag(HtmlUtil.TD);

        writer.element(HtmlUtil.TD, defaultValue);
        if (required) {
          writer.element(HtmlUtil.TD, "Yes");
        } else {
          writer.element(HtmlUtil.TD, "No");
        }
        DocletUtil.descriptionTd(writer, method.containingClass(),
          descriptions, name);
        writer.endTag(HtmlUtil.TR);
      }
      writer.endTag(HtmlUtil.TBODY);

      writer.endTag(HtmlUtil.TABLE);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  private void requestMethods(final AnnotationDesc requestMapping) {
    final AnnotationValue[] methods = getElementValue(requestMapping, "method");
    if (methods != null && methods.length > 0) {
      writer.element(HtmlUtil.H4, "HTTP Request Methods");
      writer.element(HtmlUtil.P,
        "The resource can be accessed using the following HTTP request methods.");
      writer.startTag(HtmlUtil.UL);
      for (final AnnotationValue value : methods) {
        final FieldDoc method = (FieldDoc)value.value();
        writer.element(HtmlUtil.LI, method.name());
      }
      writer.endTag(HtmlUtil.UL);
    }
  }

  private void responseStatus(final MethodDoc method) {
    final Map<String, List<String>> responseStatusDescriptions = new TreeMap<String, List<String>>();

    for (final Tag tag : method.tags()) {
      if (tag.name().equals("@web.response.status")) {
        final String text = tag.text();

        final int index = text.indexOf(" ");
        if (index != -1) {
          final String status = text.substring(0, index);
          final String description = text.substring(index + 1).trim();
          addResponseStatusDescription(responseStatusDescriptions, status,
            description);
        }
      }
    }
    addResponseStatusDescription(
      responseStatusDescriptions,
      "500",
      "<p><b>Internal Server Error</b></p>"
        + "<p>This error indicates that there was an unexpected error on the server. "
        + "This is sometimes temporary so try again after a few minutes. "
        + "The problem could also be caused by bad input data so verify all input parameters and files. "
        + "If the problem persists contact the support desk with exact details of the parameters you were using.</p>");
    if (!responseStatusDescriptions.isEmpty()) {
      writer.element(HtmlUtil.H4, "HTTP Status Codes");
      writer.element(
        HtmlUtil.P,
        "The resource will return one of the following status codes. The HTML error page may include an error message. The descriptions of the messages and the cause are described below.");
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable");

      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");

      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "HTTP Status Code");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTag(HtmlUtil.TR);
      writer.endTag(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (final Entry<String, List<String>> entry : responseStatusDescriptions.entrySet()) {
        final String code = entry.getKey();
        for (final String message : entry.getValue()) {
          writer.startTag(HtmlUtil.TR);
          writer.element(HtmlUtil.TD, code);
          writer.startTag(HtmlUtil.TD);
          writer.write(message);
          writer.endTag(HtmlUtil.TD);

          writer.endTag(HtmlUtil.TR);
        }
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
      } else if (optionName.equals("-docid")) {
        docId = option[1];
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

        head();
        writer.startTag(HtmlUtil.BODY);
      } else {
        header = header.replaceAll("\\$\\{docTitle\\}", docTitle);
        header = header.replaceAll("\\$\\{docId\\}", docId);
        writer.write(header);
      }

      documentation();

      if (footer == null) {
        writer.endTag(HtmlUtil.BODY);

        writer.endTag(HtmlUtil.HTML);
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

  private void uriTemplateParameters(final MethodDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      if (DocletUtil.hasAnnotation(parameter.annotations(),
        "org.springframework.web.bind.annotation.PathVariable")) {
        parameters.add(parameter);
      }
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);
      writer.element(HtmlUtil.H4, "URI Template Parameters");
      writer.element(
        HtmlUtil.P,
        "The URI templates support the following parameters which must be replaced with values as described below.");
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable");

      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");

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
        writer.element(HtmlUtil.TD, "{" + name + "}");
        writer.element(HtmlUtil.TD, parameter.typeName());
        DocletUtil.descriptionTd(writer, method.containingClass(),
          descriptions, name);

        writer.endTag(HtmlUtil.TR);
      }
      writer.endTag(HtmlUtil.TBODY);

      writer.endTag(HtmlUtil.TABLE);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  public void uriTemplates(final AnnotationDesc requestMapping) {
    final AnnotationValue[] uriTemplates = getElementValue(requestMapping,
      "value");
    if (uriTemplates.length > 0) {
      writer.element(HtmlUtil.H4, "URI Templates");
      writer.element(
        HtmlUtil.P,
        "The URI templates define the paths that can be appended to the base URL of the service to access this resource.");

      for (final AnnotationValue uriTemplate : uriTemplates) {
        writer.element(HtmlUtil.PRE, uriTemplate.value());
      }
    }
  }

}
