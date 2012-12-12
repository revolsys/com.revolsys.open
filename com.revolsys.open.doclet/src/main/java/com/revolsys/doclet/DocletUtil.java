package com.revolsys.doclet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.doclet.client.ClientDoclet;
import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import com.sun.javadoc.WildcardType;

public class DocletUtil {

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

  public static String qualifiedName(ProgramElementDoc element) {
    String packageName = element.containingPackage().name();
    return packageName + "." + element.name();
  }

  public static boolean hasAnnotation(ProgramElementDoc doc, String name) {
    AnnotationDesc annotation = getAnnotation(doc, name);
    return annotation != null;
  }

  public static boolean hasAnnotation(AnnotationDesc[] annotations, String name) {
    AnnotationDesc annotation = getAnnotation(annotations, name);
    return annotation != null;
  }

  public static Map<String, String> getParameterDescriptions(
    ExecutableMemberDoc method) {
    Map<String, String> descriptions = new HashMap<String, String>();
    for (ParamTag tag : method.paramTags()) {
      descriptions.put(tag.parameterName(), tag.parameterComment());
    }
    return descriptions;
  }

  public static void typeNameLink(XmlWriter writer, final Type type) {
    if (type instanceof WildcardType) {
      WildcardType wildCard = (WildcardType)type;
      writer.text("?");
      Type[] extendsBounds = wildCard.extendsBounds();
      if (extendsBounds.length > 0) {
        writer.text(" extends ");
        for (int i = 0; i < extendsBounds.length; i++) {
          if (i > 0) {
            writer.text(", ");
          }
          Type extendsType = extendsBounds[i];
          typeNameLink(writer, extendsType);
        }
      }
    } else {
      final String qualifiedTypeName = type.qualifiedTypeName();
      if (qualifiedTypeName.startsWith("java.")) {
        final String url = "http://docs.oracle.com/javase/6/docs/api/"
          + qualifiedTypeName.replaceAll("\\.", "/") + ".html?is-external=true";
        HtmlUtil.serializeA(writer, "", url, type.typeName());
      } else {
        writer.text(type);
      }
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType)type;
        Type[] typeArguments = parameterizedType.typeArguments();
        if (typeArguments.length > 0) {
          writer.text("<");
          for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
              writer.text(", ");
            }
            Type typeParameter = typeArguments[i];
            typeNameLink(writer, typeParameter);
          }
          writer.text(">");
        }
      }
    }
    writer.text(type.dimension());
  }

  public static void typeName(XmlWriter writer, final Type type) {
    String typeName = type.qualifiedTypeName();
    typeName = typeName.replaceAll("^java.lang.", "");
    typeName = typeName.replaceAll("^java.io.", "");
    typeName = typeName.replaceAll("^java.util.", "");
    writer.text(typeName);
    writer.text(type.dimension());
  }

  public static void title(XmlWriter writer, final String name,
    final String title) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, name);
    writer.text(title);
    writer.endTagLn(HtmlUtil.A);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static void title(final XmlWriter writer, final String title) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.text(title);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static void head(XmlWriter writer, String docTitle) {
    writer.startTag(HtmlUtil.HEAD);
    writer.element(HtmlUtil.TITLE, docTitle);
    HtmlUtil.serializeCss(
      writer,
      "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/css/jquery.dataTables_themeroller.css");
    HtmlUtil.serializeCss(
      writer,
      "http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/themes/cupertino/jquery-ui.css");
    HtmlUtil.serializeCss(writer, "prettify.css");
    HtmlUtil.serializeCss(writer, "javadoc.css");
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js");
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/jquery-ui.min.js");
    HtmlUtil.serializeScriptLink(
      writer,
      "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/jquery.dataTables.min.js");
    HtmlUtil.serializeScriptLink(writer, "prettify.js");
    HtmlUtil.serializeScriptLink(writer, "javadoc.js");
    writer.endTagLn(HtmlUtil.HEAD);
  }

  public static void copyFiles(String destDir) {
    for (String name : Arrays.asList("javadoc.css", "javadoc.js", "javadoc.js", "prettify.js", "prettify.css")) {
      FileUtil.copy(
        DocletUtil.class.getResourceAsStream("/com/revolsys/doclet/" + name),
        new File(destDir, name));
    }
  }

}
