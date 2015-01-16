package com.revolsys.doclet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.WildcardType;

public class DocletUtil {

  public static void addPackageUrl(final String packagePrefix, final String url) {
    PACKAGE_URLS.put(packagePrefix, url);
  }

  public static void anchor(final XmlWriter writer, final String name,
    final String title) {
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, name);
    writer.text(title);
    writer.endTagLn(HtmlUtil.A);
  }

  public static void copyFiles(final String destDir) {
    for (final String name : Arrays.asList("javadoc.css", "javadoc.js",
      "javadoc.js", "prettify.js", "prettify.css")) {
      FileUtil.copy(
        DocletUtil.class.getResourceAsStream("/com/revolsys/doclet/" + name),
        new File(destDir, name));
    }
  }

  public static void description(final XmlWriter writer,
    final ClassDoc containingClass, final Doc doc) {
    final Tag[] tags = doc.inlineTags();
    description(writer, containingClass, tags);
  }

  public static void description(final XmlWriter writer,
    final ClassDoc containingClass, final Tag[] tags) {
    if (tags != null && tags.length > 0) {
      for (final Tag tag : tags) {
        final String kind = tag.kind();
        if (tag instanceof SeeTag) {
          final SeeTag seeTag = (SeeTag)tag;
          seeTag(writer, containingClass, seeTag);
        } else if ("Text".equals(kind)) {
          writer.write(tag.text());
        }
      }
    }
  }

  public static void descriptionTd(final XmlWriter writer,
    final ClassDoc containingClass, final Map<String, Tag[]> descriptions,
    final String name) {
    writer.startTag(HtmlUtil.TD);
    writer.attribute(HtmlUtil.ATTR_CLASS, "description");
    final Tag[] description = descriptions.get(name);
    description(writer, containingClass, description);
    writer.endTagLn(HtmlUtil.TD);
  }

  public static void documentationReturn(final XmlWriter writer,
    final MethodDoc method) {
    final Type type = method.returnType();
    if (type != null && !"void".equals(type.qualifiedTypeName())) {
      Tag[] descriptionTags = null;
      for (final Tag tag : method.tags()) {
        if (tag.name().equals("@return")) {
          descriptionTags = tag.inlineTags();
        }
      }
      title(writer, "Return");

      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "simpleDataTable parameters");
      writer.startTag(HtmlUtil.TABLE);
      writer.attribute(HtmlUtil.ATTR_CLASS, "data");
      writer.startTag(HtmlUtil.THEAD);
      writer.startTag(HtmlUtil.TR);
      writer.element(HtmlUtil.TH, "Type");
      writer.element(HtmlUtil.TH, "Description");
      writer.endTagLn(HtmlUtil.TR);
      writer.endTagLn(HtmlUtil.THEAD);

      writer.startTag(HtmlUtil.TBODY);

      writer.startTag(HtmlUtil.TR);
      writer.startTag(HtmlUtil.TD);
      writer.attribute(HtmlUtil.ATTR_CLASS, "type");
      typeNameLink(writer, type);
      writer.endTagLn(HtmlUtil.TD);

      writer.startTag(HtmlUtil.TD);
      writer.attribute(HtmlUtil.ATTR_CLASS, "description");
      description(writer, method.containingClass(), descriptionTags);
      writer.endTagLn(HtmlUtil.TD);

      writer.endTagLn(HtmlUtil.TR);

      writer.endTagLn(HtmlUtil.TBODY);

      writer.endTagLn(HtmlUtil.TABLE);
      writer.endTagLn(HtmlUtil.DIV);
    }
  }

  public static AnnotationDesc getAnnotation(
    final AnnotationDesc[] annotations, final String name) {
    for (final AnnotationDesc annotation : annotations) {
      final AnnotationTypeDoc annotationType = annotation.annotationType();
      final String annotationName = qualifiedName(annotationType);
      if (name.equals(annotationName)) {
        return annotation;
      }
    }
    return null;
  }

  public static AnnotationDesc getAnnotation(final ProgramElementDoc doc,
    final String name) {
    final AnnotationDesc[] annotations = doc.annotations();
    return getAnnotation(annotations, name);
  }

  public static String getExternalUrl(final String qualifiedTypeName) {
    for (final Entry<String, String> entry : PACKAGE_URLS.entrySet()) {
      final String packagePrefix = entry.getKey();
      if (qualifiedTypeName.startsWith(packagePrefix)) {
        final String baseUrl = entry.getValue();
        final String url = baseUrl + qualifiedTypeName.replaceAll("\\.", "/")
            + ".html?is-external=true";
        return url;
      }
    }
    return null;
  }

  public static Map<String, Tag[]> getParameterDescriptions(
    final ExecutableMemberDoc method) {
    final Map<String, Tag[]> descriptions = new HashMap<String, Tag[]>();
    for (final ParamTag tag : method.paramTags()) {
      final String parameterName = tag.parameterName();
      final Tag[] commentTags = tag.inlineTags();
      descriptions.put(parameterName, commentTags);
    }
    return descriptions;
  }

  public static boolean hasAnnotation(final AnnotationDesc[] annotations,
    final String name) {
    final AnnotationDesc annotation = getAnnotation(annotations, name);
    return annotation != null;
  }

  public static boolean hasAnnotation(final ProgramElementDoc doc,
    final String name) {
    final AnnotationDesc annotation = getAnnotation(doc, name);
    return annotation != null;
  }

  public static void head(final XmlWriter writer, final String docTitle) {
    writer.startTag(HtmlUtil.HEAD);
    writer.element(HtmlUtil.TITLE, docTitle);
    for (final String url : Arrays.asList(
      "https://code.jquery.com/ui/1.11.2/themes/cupertino/jquery-ui.css",
      "https://cdn.datatables.net/1.10.4/css/jquery.dataTables.min.css",
      "prettify.css", "javadoc.css")) {
      HtmlUtil.serializeCss(writer, url);

    }
    for (final String url : Arrays.asList(
      "https://code.jquery.com/jquery-1.11.1.min.js",
      "https://code.jquery.com/ui/1.11.2/jquery-ui.min.js",
      "https://cdn.datatables.net/1.10.4/js/jquery.dataTables.min.js",
      "prettify.js", "javadoc.js")) {
      HtmlUtil.serializeScriptLink(writer, url);
    }
    writer.endTagLn(HtmlUtil.HEAD);
  }

  public static boolean isTypeIncluded(final Type type) {
    final ClassDoc classDoc = type.asClassDoc();
    final ClassDoc annotationDoc = type.asAnnotationTypeDoc();
    final boolean included = annotationDoc != null
        && annotationDoc.isIncluded() || classDoc != null
        && classDoc.isIncluded();
    return included;
  }

  public static void label(final XmlWriter writer, final String label,
    final boolean code) {
    if (code) {
      writer.startTag(HtmlUtil.CODE);
    }
    writer.text(label);
    if (code) {
      writer.endTag(HtmlUtil.CODE);
    }
  }

  public static void link(final XmlWriter writer, final String url,
    final String label, final boolean code) {
    final boolean hasUrl = StringUtils.hasText(url);
    if (hasUrl) {
      writer.startTag(HtmlUtil.A);
      writer.attribute(HtmlUtil.ATTR_HREF, url);
    }
    label(writer, label, code);
    if (hasUrl) {
      writer.endTag(HtmlUtil.A);
    }
  }

  public static String qualifiedName(final ProgramElementDoc element) {
    final String packageName = element.containingPackage().name();
    return packageName + "." + element.name();
  }

  public static String replaceDocRootDir(final String text) {
    int i = text.indexOf("{@");
    if (i < 0) {
      return text;
    } else {
      final String lowerText = text.toLowerCase();
      i = lowerText.indexOf("{@docroot}", i);
      if (i < 0) {
        return text;
      } else {
        final StringBuffer stringbuffer = new StringBuffer();
        int k = 0;
        do {
          final int j = lowerText.indexOf("{@docroot}", k);
          if (j < 0) {
            stringbuffer.append(text.substring(k));
            break;
          }
          stringbuffer.append(text.substring(k, j));
          k = j + 10;
          stringbuffer.append("./");
          if ("./".length() > 0 && k < text.length() && text.charAt(k) != '/') {
            stringbuffer.append("/");
          }
        } while (true);
        return stringbuffer.toString();
      }
    }
  }

  public static void seeTag(final XmlWriter writer,
    final ClassDoc containingClass, final SeeTag seeTag) {
    final String name = seeTag.name();
    if (name.startsWith("@link") || name.equals("@see")) {
      final boolean code = !name.equalsIgnoreCase("@linkplain");
      String label = seeTag.label();

      final StringBuffer stringbuffer = new StringBuffer();

      final String seeTagText = replaceDocRootDir(seeTag.text());
      if (seeTagText.startsWith("<") || seeTagText.startsWith("\"")) {
        stringbuffer.append(seeTagText);
        writer.write(seeTagText);
      } else {
        final ClassDoc referencedClass = seeTag.referencedClass();
        final MemberDoc referencedMember = seeTag.referencedMember();
        String referencedMemberName = seeTag.referencedMemberName();
        if (referencedClass == null) {
          final PackageDoc packagedoc = seeTag.referencedPackage();
          if (packagedoc != null && packagedoc.isIncluded()) {
            final String packageName = packagedoc.name();
            if (!StringUtils.hasText(label)) {
              label = packageName;
            }
            link(writer, "#" + packageName, label, code);
          } else {
            // TODO link to external package or class
            // String s9 = getCrossPackageLink(referencedClassName);
            // String s8;
            // if (s9 != null)
            // stringbuffer.append(getHyperLink(s9, "", s1.length() != 0 ? s1
            // : s3, false));
            // else if ((s8 = getCrossClassLink(referencedClassName,
            // referencedMemberName, s1, false, "", !plainLink)) != null) {
            // stringbuffer.append(s8);
            // } else {
            // configuration.getDocletSpecificMsg().warning(seeTag.position(),
            // "doclet.see.class_or_package_not_found", name, s2);
            // stringbuffer.append(s1.length() != 0 ? s1 : s3);
            // }
          }
        } else {
          String url = null;
          final String className = referencedClass.qualifiedName();
          if (referencedClass.isIncluded()) {
            url = "#" + className;
          } else {
            url = getExternalUrl(className);
            if (!StringUtils.hasText(url)) {
              label = className;
            }
          }
          if (referencedMember != null) {
            if (referencedMember instanceof ExecutableMemberDoc) {
              if (referencedMemberName.indexOf('(') < 0) {
                final ExecutableMemberDoc executableDoc = (ExecutableMemberDoc)referencedMember;
                referencedMemberName = referencedMemberName
                    + executableDoc.signature();
              }
              if (StringUtils.hasText(referencedMemberName)) {
                label = referencedMemberName;
              } else {
                label = seeTagText;
              }
            }
            if (referencedClass.isIncluded()) {
              url += "." + referencedMemberName;
            } else if (StringUtils.hasText(url)) {
              url += "#" + referencedMemberName;
            } else {
              label = referencedMember.toString();
            }
          }
          if (!StringUtils.hasText(label)) {
            label = referencedClass.name();
          }
          link(writer, url, label, code);
        }
      }
    }
  }

  public static void tagWithAnchor(final XmlWriter writer, final QName tag,
    final String name, final String title) {
    writer.startTag(tag);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_NAME, name);
    writer.text(title);
    writer.endTag(HtmlUtil.A);
    writer.endTag(tag);
  }

  public static void title(final XmlWriter writer, final String title) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    writer.text(title);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static void title(final XmlWriter writer, final String name,
    final String title) {
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "title");
    anchor(writer, name, title);
    writer.endTagLn(HtmlUtil.DIV);
  }

  public static void typeName(final XmlWriter writer, final Type type) {
    String typeName;
    final String qualifiedTypeName = type.qualifiedTypeName();
    if (isTypeIncluded(type) || getExternalUrl(qualifiedTypeName) != null) {
      typeName = type.typeName();
    } else {
      typeName = qualifiedTypeName;
    }
    writer.text(typeName);
    writer.text(type.dimension());
  }

  public static void typeNameLink(final XmlWriter writer, final Type type) {
    if (type instanceof WildcardType) {
      final WildcardType wildCard = (WildcardType)type;
      writer.text("?");
      final Type[] extendsBounds = wildCard.extendsBounds();
      if (extendsBounds.length > 0) {
        writer.text(" extends ");
        for (int i = 0; i < extendsBounds.length; i++) {
          if (i > 0) {
            writer.text(", ");
          }
          final Type extendsType = extendsBounds[i];
          typeNameLink(writer, extendsType);
        }
      }
    } else {
      final String qualifiedTypeName = type.qualifiedTypeName();
      final String externalLink = getExternalUrl(qualifiedTypeName);

      final boolean included = isTypeIncluded(type);

      if (externalLink != null) {
        HtmlUtil.serializeA(writer, "", externalLink, type.typeName());
      } else if (included) {
        final String url = "#" + qualifiedTypeName;
        HtmlUtil.serializeA(writer, "", url, type.typeName());
      } else {
        writer.text(qualifiedTypeName);
      }
      if (type instanceof ParameterizedType) {
        final ParameterizedType parameterizedType = (ParameterizedType)type;
        final Type[] typeArguments = parameterizedType.typeArguments();
        if (typeArguments.length > 0) {
          writer.text("<");
          for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
              writer.text(", ");
            }
            final Type typeParameter = typeArguments[i];
            typeNameLink(writer, typeParameter);
          }
          writer.text(">");
        }
      }
    }
    writer.text(type.dimension());
  }

  private static final Map<String, String> PACKAGE_URLS = new LinkedHashMap<String, String>();

  static {
    addPackageUrl("java.", "http://docs.oracle.com/javase/6/docs/api/");
    addPackageUrl("com.revolsys.jts.",
        "http://tsusiatsoftware.net/jts/javadoc/");
  }
}
