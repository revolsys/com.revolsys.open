package com.revolsys.doclet.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class RestDoclet {

  public static AnnotationDesc getAnnotation(ProgramElementDoc doc, String name) {
    AnnotationDesc[] annotations = doc.annotations();
    return getAnnotation(annotations, name);
  }

  public static AnnotationDesc getAnnotation(AnnotationDesc[] annotations,
    String name) {
    for (AnnotationDesc annotation : annotations) {
      AnnotationTypeDoc annotationType = annotation.annotationType();
      String annotationName = qualifiedName(annotationType);
      if (name.equals(annotationName)) {
        return annotation;
      }
    }
    return null;
  }

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d") || optionName.equals("-doctitle")) {
      return 2;
    }
    return -1;
  }

  public static String qualifiedName(ProgramElementDoc element) {
    String packageName = element.containingPackage().name();
    return packageName + "." + element.name();
  }

  public static boolean start(RootDoc root) {
    new RestDoclet(root).start();

    return true;
  }

  public static boolean validOptions(String args[][],
    DocErrorReporter docerrorreporter) {
    boolean flag = true;
    String s = "";
    for (String[] arg : args) {
      String argName = arg[0].toLowerCase();
      if (argName.equals("-d")) {
        String destDir = arg[1];
        File file = new File(destDir);
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
      }
    }

    return flag || s.length() <= 0;
  }

  private String docTitle;

  private RootDoc root;

  private XmlWriter writer;

  private String destDir = ".";

  public RestDoclet(RootDoc root) {
    this.root = root;
  }

  private void setOptions(String[][] options) {
    for (String[] option : options) {
      String optionName = option[0];
      if (optionName.equals("-d")) {
        destDir = option[1];

      } else if (optionName.equals("-doctitle")) {
        docTitle = option[1];
      } else {
      }
    }
    try {
      File dir = new File(destDir);
      File indexFile = new File(dir, "index.html");
      FileWriter out = new FileWriter(indexFile);
      writer = new XmlWriter(out, false);
      FileUtil.copy(getClass().getResourceAsStream("javadoc.css"), new File(
        destDir, "javadoc.css"));
      FileUtil.copy(getClass().getResourceAsStream("javadoc.js"), new File(
        destDir, "javadoc.js"));
    } catch (IOException e) {
      throw new IllegalArgumentException(e.fillInStackTrace().getMessage(), e);
    }
  }

  private void start() {
    try {
      setOptions(root.options());

      writer.startDocument("UTF-8", "1.0");
      writer.docType("html", null);
      writer.startTag(HtmlUtil.HTML);
      writer.attribute(HtmlUtil.ATTR_LANG, "en");

      head();

      body();

      writer.endTag(HtmlUtil.HTML);
      writer.endDocument();
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public static boolean hasAnnotation(ProgramElementDoc doc, String name) {
    AnnotationDesc annotation = getAnnotation(doc, name);
    return annotation != null;
  }

  public static boolean hasAnnotation(AnnotationDesc[] annotations, String name) {
    AnnotationDesc annotation = getAnnotation(annotations, name);
    return annotation != null;
  }

  public void body() {
    writer.startTag(HtmlUtil.BODY);

    writer.element(HtmlUtil.H1, docTitle);

    documentation();

    writer.endTag(HtmlUtil.BODY);
  }

  public void documentation() {
    writer.startTag(HtmlUtil.DIV);
    for (PackageDoc packageDoc : root.specifiedPackages()) {
      Map<String, ClassDoc> classes = new TreeMap<String, ClassDoc>();
      for (ClassDoc classDoc : packageDoc.ordinaryClasses()) {
        classes.put(classDoc.name(), classDoc);
      }
      for (ClassDoc classDoc : classes.values()) {
        documentationClass(classDoc);
      }
    }

    writer.endTag(HtmlUtil.DIV);
  }

  public void documentationClass(ClassDoc classDoc) {
    if (hasAnnotation(classDoc, "org.springframework.stereotype.Controller")) {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "restClass");
      writer.attribute(HtmlUtil.ATTR_ID, qualifiedName(classDoc));
      String name = classDoc.name();
      writer.element(HtmlUtil.H2, CaseConverter.toCapitalizedWords(name));
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "content");
      writer.write(classDoc.commentText());
      documentationMethod(classDoc);
      writer.endTag(HtmlUtil.DIV);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  public void documentationMethod(ClassDoc classDoc) {
    for (MethodDoc method : classDoc.methods()) {
      String methodName = method.name();
      AnnotationDesc requestMapping = getAnnotation(method,
        "org.springframework.web.bind.annotation.RequestMapping");
      if (requestMapping != null) {
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "restMethod");
        writer.attribute(HtmlUtil.ATTR_ID, qualifiedName(classDoc) + "."
          + methodName);
        writer.element(HtmlUtil.H3,
          CaseConverter.toCapitalizedWords(methodName));
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "content");
        writer.write(method.commentText());

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

  private void responseStatus(MethodDoc method) {
    Map<String, List<String>> responseStatusDescriptions = new TreeMap<String, List<String>>();

    for (Tag tag : method.tags()) {
      if (tag.name().equals("@web.response.status")) {
        String text = tag.text();

        int index = text.indexOf(" ");
        if (index != -1) {
          String status = text.substring(0, index);
          String description = text.substring(index + 1).trim();
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
      writer.attribute(HtmlUtil.ATTR_STYLE, "width:auto;margin-left:0px");

      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "HTTP Status Code");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTag(HtmlUtil.TR);
      writer.endTag(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);
      for (Entry<String, List<String>> entry : responseStatusDescriptions.entrySet()) {
        String code = entry.getKey();
        for (String message : entry.getValue()) {
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

  public void addResponseStatusDescription(
    Map<String, List<String>> responseCodes, String code, String description) {
    List<String> descriptions = responseCodes.get(code);
    if (descriptions == null) {
      descriptions = new ArrayList<String>();
      responseCodes.put(code, descriptions);
    }
    descriptions.add(description);
  }

  private void requestMethods(AnnotationDesc requestMapping) {
    AnnotationValue[] methods = getElementValue(requestMapping, "method");
    if (methods != null && methods.length > 0) {
      writer.element(HtmlUtil.H4, "HTTP Request Methods");
      writer.element(HtmlUtil.P,
        "The resource can be accessed using the following HTTP request methods.");
      writer.startTag(HtmlUtil.UL);
      for (AnnotationValue value : methods) {
        FieldDoc method = (FieldDoc)value.value();
        writer.element(HtmlUtil.LI, method.name());
      }
      writer.endTag(HtmlUtil.UL);
    }
  }

  public void uriTemplates(AnnotationDesc requestMapping) {
    AnnotationValue[] uriTemplates = getElementValue(requestMapping, "value");
    if (uriTemplates.length > 0) {
      writer.element(HtmlUtil.H4, "URI Templates");
      writer.element(
        HtmlUtil.P,
        "The URI templates define the paths that can be appended to the base URL of the service to access this resource.");

      for (AnnotationValue uriTemplate : uriTemplates) {
        writer.element(HtmlUtil.PRE, uriTemplate.value());
      }
    }
  }

  private void uriTemplateParameters(MethodDoc method) {
    List<Parameter> parameters = new ArrayList<Parameter>();
    for (Parameter parameter : method.parameters()) {
      if (hasAnnotation(parameter.annotations(),
        "org.springframework.web.bind.annotation.PathVariable")) {
        parameters.add(parameter);
      }
    }
    if (!parameters.isEmpty()) {
      Map<String, String> descriptions = getParameterDescriptions(method);
      writer.element(HtmlUtil.H4, "URI Template Parameters");
      writer.element(
        HtmlUtil.P,
        "The URI templates support the following parameters which must be replaced with values as described below.");
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
      for (Parameter parameter : parameters) {
        writer.startTag(HtmlUtil.TR);
        String name = parameter.name();
        writer.element(HtmlUtil.TD, "{" + name + "}");
        writer.element(HtmlUtil.TD, parameter.typeName());
        description(descriptions, name);

        writer.endTag(HtmlUtil.TR);
      }
      writer.endTag(HtmlUtil.TBODY);

      writer.endTag(HtmlUtil.TABLE);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  public void description(Map<String, String> descriptions, String name) {
    writer.startTag(HtmlUtil.TD);
    String description = descriptions.get(name);
    if (description == null) {
      writer.write("-");
    } else {
      writer.write(description);
    }
    writer.endTag(HtmlUtil.TD);
  }

  public static Map<String, String> getParameterDescriptions(MethodDoc method) {
    Map<String, String> descriptions = new HashMap<String, String>();
    for (ParamTag tag : method.paramTags()) {
      descriptions.put(tag.parameterName(), tag.parameterComment());
    }
    return descriptions;
  }

  private void parameters(MethodDoc method) {
    List<Parameter> parameters = new ArrayList<Parameter>();
    for (Parameter parameter : method.parameters()) {
      AnnotationDesc[] annotations = parameter.annotations();
      if (hasAnnotation(annotations,
        "org.springframework.web.bind.annotation.RequestParam")
        || hasAnnotation(annotations,
          "org.springframework.web.bind.annotation.RequestBody")) {
        parameters.add(parameter);
      }
    }
    if (!parameters.isEmpty()) {
      Map<String, String> descriptions = getParameterDescriptions(method);

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
      writer.attribute(HtmlUtil.ATTR_STYLE, "width:auto;margin-left:0px");
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
      for (Parameter parameter : parameters) {
        writer.startTag(HtmlUtil.TR);
        String name = parameter.name();
        AnnotationDesc requestParam = getAnnotation(parameter.annotations(),
          "org.springframework.web.bind.annotation.RequestParam");
        AnnotationDesc requestBody = getAnnotation(parameter.annotations(),
          "org.springframework.web.bind.annotation.RequestBody");
        String paramName = name;
        String defaultValue = "-";
        String typeName = parameter.typeName().replaceFirst("^java.lang.", "");
        boolean required = true;
        if (requestParam != null) {
          String value = getElementValue(requestParam, "value");
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
          paramName="HTTP Request body or 'body' parameter";
          typeName="binary/character data";
        }
        writer.element(HtmlUtil.TD, paramName);
         writer.element(HtmlUtil.TD,
          typeName);
        writer.element(HtmlUtil.TD, defaultValue);
        if (required) {
          writer.element(HtmlUtil.TD, "Yes");
        } else {
          writer.element(HtmlUtil.TD, "No");
        }
        description(descriptions, name);
        writer.endTag(HtmlUtil.TR);
      }
      writer.endTag(HtmlUtil.TBODY);

      writer.endTag(HtmlUtil.TABLE);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  private <T> T getElementValue(AnnotationDesc annotation, String name) {
    for (ElementValuePair pair : annotation.elementValues()) {
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

}
