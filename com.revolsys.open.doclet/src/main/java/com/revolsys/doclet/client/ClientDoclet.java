package com.revolsys.doclet.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.doclet.BaseDoclet;
import com.revolsys.doclet.DocletUtil;
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

public class ClientDoclet extends BaseDoclet {
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static int optionLength(final String optionName) {
    return DocletUtil.optionLength(optionName);
  }

  public static boolean start(final RootDoc root) {
    new ClientDoclet(root).start();
    return true;
  }

  public static boolean validOptions(final String options[][],
    final DocErrorReporter docerrorreporter) {
    return DocletUtil.validOptions(options, docerrorreporter);
  }

  public ClientDoclet(final RootDoc root) {
    super(root);
  }

  public void addResponseStatusDescription(final Map<String, List<String>> responseCodes,
    final String code, final String description) {
    List<String> descriptions = responseCodes.get(code);
    if (descriptions == null) {
      descriptions = new ArrayList<String>();
      responseCodes.put(code, descriptions);
    }
    descriptions.add(description);
  }

  @Override
  public void documentation() {
    DocletUtil.contentContainer(this.writer, "col-md-12");

    this.writer.element(HtmlUtil.H1, this.docTitle);
    DocletUtil.description(this.writer, null, this.root);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      documentationPackage(packageDoc);
    }
    DocletUtil.endContentContainer(this.writer);
  }

  public void documentationAnnotation(final AnnotationTypeDoc annotationDoc) {
    final String name = annotationDoc.name();

    final String id = getClassId(annotationDoc);

    DocletUtil.panelStart(this.writer, "panel-primary", HtmlUtil.H3, id, "annotation", name, null);

    DocletUtil.description(this.writer, annotationDoc, annotationDoc);

    final AnnotationTypeElementDoc[] elements = annotationDoc.elements();
    if (elements.length > 0) {

      this.writer.startTag(HtmlUtil.DIV);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "table-responsive parameters");
      this.writer.startTag(HtmlUtil.TABLE);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "table table-striped table-bordered");
      this.writer.startTag(HtmlUtil.THEAD);
      this.writer.startTag(HtmlUtil.TR);
      this.writer.element(HtmlUtil.TH, "Name");
      this.writer.element(HtmlUtil.TH, "Type");
      this.writer.element(HtmlUtil.TH, "Default");
      this.writer.startTag(HtmlUtil.TH);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlUtil.TH);
      this.writer.endTagLn(HtmlUtil.TR);
      this.writer.endTagLn(HtmlUtil.THEAD);

      this.writer.startTag(HtmlUtil.TBODY);
      for (final AnnotationTypeElementDoc element : elements) {
        this.writer.startTag(HtmlUtil.TR);
        final String elementName = element.name();

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        DocletUtil.anchor(this.writer, id + "." + elementName, elementName);
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "type");
        DocletUtil.typeNameLink(this.writer, element.returnType());
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "default");
        final AnnotationValue defaultValue = element.defaultValue();
        if (defaultValue == null) {
          this.writer.text("-");
        } else {
          this.writer.text(defaultValue);
        }
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
        DocletUtil.description(this.writer, null, element);
        this.writer.endTagLn(HtmlUtil.TD);
        this.writer.endTagLn(HtmlUtil.TR);
      }
      this.writer.endTagLn(HtmlUtil.TBODY);

      this.writer.endTagLn(HtmlUtil.TABLE);
      this.writer.endTagLn(HtmlUtil.DIV);

    }
    DocletUtil.panelEnd(this.writer);
  }

  public void documentationAnnotations(final PackageDoc packageDoc) {
    final Map<String, AnnotationTypeDoc> annotations = BaseDoclet.getAnnotations(packageDoc);
    if (!annotations.isEmpty()) {
      for (final AnnotationTypeDoc annotationDoc : annotations.values()) {
        documentationAnnotation(annotationDoc);
      }
    }
  }

  public void documentationClass(final String classType, final ClassDoc classDoc) {
    final String id = getClassId(classDoc);
    final String name = classDoc.name();

    DocletUtil.panelStart(this.writer, "panel-primary", HtmlUtil.H3, id, classType, name, null);

    DocletUtil.description(this.writer, classDoc, classDoc);

    final ConstructorDoc[] constructors = classDoc.constructors();
    if (constructors.length > 0) {
      for (final ConstructorDoc method : constructors) {
        documentationMethod(method);
      }
    }

    final MethodDoc[] methods = classDoc.methods();
    if (methods.length > 0) {
      for (final MethodDoc method : methods) {
        documentationMethod(method);
      }
    }
    DocletUtil.panelEnd(this.writer);
  }

  public void documentationClasses(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> classes = BaseDoclet.getClasses(packageDoc);
    if (!classes.isEmpty()) {
      for (final ClassDoc classDoc : classes.values()) {
        documentationClass("class", classDoc);
      }
    }
  }

  public void documentationEnum(final ClassDoc enumDoc) {
    final String id = getClassId(enumDoc);
    final String name = enumDoc.name();

    DocletUtil.panelStart(this.writer, "panel-primary", HtmlUtil.H3, id, "enum", name, null);
    DocletUtil.description(this.writer, enumDoc, enumDoc);

    final FieldDoc[] elements = enumDoc.enumConstants();
    if (elements.length > 0) {
      this.writer.startTag(HtmlUtil.DIV);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "table-responsive parameters");
      this.writer.startTag(HtmlUtil.TABLE);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "table table-striped table-bordered");
      this.writer.startTag(HtmlUtil.THEAD);
      this.writer.startTag(HtmlUtil.TR);
      this.writer.element(HtmlUtil.TH, "Constant");
      this.writer.startTag(HtmlUtil.TH);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlUtil.TH);
      this.writer.endTagLn(HtmlUtil.TR);
      this.writer.endTagLn(HtmlUtil.THEAD);

      this.writer.startTag(HtmlUtil.TBODY);
      for (final FieldDoc element : elements) {
        this.writer.startTag(HtmlUtil.TR);
        final String elementName = element.name();

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "constant");
        this.writer.attribute(HtmlUtil.ATTR_ID, id + "_" + elementName);
        HtmlUtil.elementWithId(this.writer, HtmlUtil.SPAN, id + "." + elementName, elementName);
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
        DocletUtil.description(this.writer, null, element);
        this.writer.endTagLn(HtmlUtil.TD);
        this.writer.endTagLn(HtmlUtil.TR);
      }
      this.writer.endTagLn(HtmlUtil.TBODY);

      this.writer.endTagLn(HtmlUtil.TABLE);
      this.writer.endTagLn(HtmlUtil.DIV);

    }
    DocletUtil.panelEnd(this.writer);
  }

  public void documentationEnums(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> enums = BaseDoclet.getEnums(packageDoc);
    if (!enums.isEmpty()) {
      for (final ClassDoc enumDoc : enums.values()) {
        documentationEnum(enumDoc);
      }
    }
  }

  public void documentationInterfaces(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> interfaces = BaseDoclet.getInterfaces(packageDoc);
    if (!interfaces.isEmpty()) {
      for (final ClassDoc classDoc : interfaces.values()) {
        documentationClass("interface", classDoc);
      }
    }
  }

  public void documentationMethod(final ExecutableMemberDoc member) {
    final String id = getMemberId(member);
    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "panel panel-info");

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "panel-heading");

    final String simpleId = id.replaceAll("[^a-zA-Z0-9_]", "_");
    this.writer.startTag(HtmlUtil.A);
    this.writer.attribute(HtmlUtil.ATTR_ID, id);
    this.writer.text("");
    this.writer.endTag(HtmlUtil.A);

    this.writer.startTag(HtmlUtil.H4);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "panel-title");
    this.writer.attribute(HtmlUtil.ATTR_ID, simpleId);
    methodSignature(member);
    this.writer.endTagLn(HtmlUtil.H4);

    this.writer.endTagLn(HtmlUtil.DIV);

    this.writer.startTag(HtmlUtil.DIV);
    this.writer.attribute(HtmlUtil.ATTR_CLASS, "panel-body");

    DocletUtil.description(this.writer, member.containingClass(), member);

    parameters(member);

    if (member instanceof MethodDoc) {
      final MethodDoc method = (MethodDoc)member;
      DocletUtil.documentationReturn(this.writer, method);
    }

    DocletUtil.panelEnd(this.writer);
  }

  public void documentationPackage(final PackageDoc packageDoc) {
    final String name = packageDoc.name();
    final String id = name;
    DocletUtil.panelStart(this.writer, "panel-default", HtmlUtil.H2, id, "package", name, null);

    DocletUtil.description(this.writer, null, packageDoc);

    documentationAnnotations(packageDoc);
    documentationEnums(packageDoc);
    documentationInterfaces(packageDoc);
    documentationClasses(packageDoc);

    DocletUtil.panelEnd(this.writer);
  }

  private String getAnchor(final ExecutableMemberDoc member) {
    final StringBuilder anchor = new StringBuilder();
    final ClassDoc classDoc = member.containingClass();
    final String className = getClassId(classDoc);
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
    this.writer.startTag(HtmlUtil.A);
    final String anchor = getAnchor(member);
    this.writer.attribute(HtmlUtil.ATTR_NAME, anchor);
    if (member instanceof MethodDoc) {
      this.writer.startTag(HtmlUtil.SMALL);
      final MethodDoc method = (MethodDoc)member;
      final Type returnType = method.returnType();
      DocletUtil.typeName(this.writer, returnType);
      this.writer.text(" ");
      this.writer.endTagLn(HtmlUtil.SMALL);
    }
    if (member.isStatic()) {
      this.writer.startTag(HtmlUtil.I);
    }
    this.writer.text(member.name());
    if (member.isStatic()) {
      this.writer.endTag(HtmlUtil.I);
    }
    this.writer.startTag(HtmlUtil.SMALL);
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
    this.writer.endTagLn(HtmlUtil.SMALL);
    this.writer.endTagLn(HtmlUtil.A);
  }

  @Override
  public void navbar() {
    DocletUtil.navbarStart(this.writer, this.docTitle);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      navMenus(getAnnotations(packageDoc));
      navMenus(getEnums(packageDoc));
      navMenus(getInterfaces(packageDoc));
      navMenus(getClasses(packageDoc));
    }
    DocletUtil.navbarEnd(this.writer);
  }

  public void navMenu(final ClassDoc classDoc) {
    final String name = classDoc.name();
    final String id = getClassId(classDoc);
    DocletUtil.navDropdownStart(this.writer, name, "#" + id, false);
    for (final MethodDoc methodDoc : classDoc.methods()) {
      navMenu(classDoc, methodDoc);
    }
    DocletUtil.navDropdownEnd(this.writer);
  }

  public void navMenu(final ClassDoc classDoc, final MethodDoc methodDoc) {
    final String name = methodDoc.name();
    final String id = getMemberId(methodDoc);
    DocletUtil.navMenuItem(this.writer, name, "#" + id);
  }

  protected void navMenus(final Map<String, ? extends ClassDoc> classes) {
    for (final ClassDoc classDoc : classes.values()) {
      navMenu(classDoc);
    }
  }

  private void parameters(final ExecutableMemberDoc method) {
    final List<Parameter> parameters = new ArrayList<Parameter>();
    for (final Parameter parameter : method.parameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      this.writer.startTag(HtmlUtil.DIV);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "table-responsive parameters");
      this.writer.startTag(HtmlUtil.TABLE);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "table table-striped table-bordered");
      this.writer.startTag(HtmlUtil.THEAD);
      this.writer.startTag(HtmlUtil.TR);
      this.writer.element(HtmlUtil.TH, "Parameter");
      this.writer.element(HtmlUtil.TH, "Type");
      this.writer.startTag(HtmlUtil.TH);
      this.writer.attribute(HtmlUtil.ATTR_CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlUtil.TH);
      this.writer.endTagLn(HtmlUtil.TR);
      this.writer.endTagLn(HtmlUtil.THEAD);

      this.writer.startTag(HtmlUtil.TBODY);
      for (final Parameter parameter : parameters) {
        this.writer.startTag(HtmlUtil.TR);
        final String name = parameter.name();

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "name");
        this.writer.text(parameter.name());
        this.writer.endTagLn(HtmlUtil.TD);

        this.writer.startTag(HtmlUtil.TD);
        this.writer.attribute(HtmlUtil.ATTR_CLASS, "type");

        final Type type = parameter.type();
        DocletUtil.typeNameLink(this.writer, type);
        this.writer.endTagLn(HtmlUtil.TD);

        DocletUtil.descriptionTd(this.writer, method.containingClass(), descriptions, name);
        this.writer.endTagLn(HtmlUtil.TR);
      }
      this.writer.endTagLn(HtmlUtil.TBODY);

      this.writer.endTagLn(HtmlUtil.TABLE);
      this.writer.endTagLn(HtmlUtil.DIV);
    }
  }
}
