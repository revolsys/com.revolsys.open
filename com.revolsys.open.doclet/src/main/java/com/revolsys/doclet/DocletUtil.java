package com.revolsys.doclet;

import java.util.HashMap;
import java.util.Map;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ProgramElementDoc;

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

  public static Map<String, String> getParameterDescriptions(ExecutableMemberDoc method) {
    Map<String, String> descriptions = new HashMap<String, String>();
    for (ParamTag tag : method.paramTags()) {
      descriptions.put(tag.parameterName(), tag.parameterComment());
    }
    return descriptions;
  }

}
