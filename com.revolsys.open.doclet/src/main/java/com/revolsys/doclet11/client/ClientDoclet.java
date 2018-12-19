package com.revolsys.doclet11.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.revolsys.doclet11.BaseDoclet;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.sun.source.doctree.DocTree;

public class ClientDoclet extends BaseDoclet {
  public ClientDoclet() {
    super("Client API");
  }

  public void addResponseStatusDescription(final Map<String, List<String>> responseCodes,
    final String code, final String description) {
    List<String> descriptions = responseCodes.get(code);
    if (descriptions == null) {
      descriptions = new ArrayList<>();
      responseCodes.put(code, descriptions);
    }
    descriptions.add(description);
  }

  @Override
  public void documentation() {
    contentContainer("col-md-12");

    this.writer.element(HtmlElem.H1, this.docTitle);
    // description(null, this.environment);
    for (final PackageElement PackageElement : getPackages()) {
      documentationPackage(PackageElement);
    }
    endContentContainer();
  }

  public void documentationAnnotation(final TypeElement annotationDoc) {
    final Name name = annotationDoc.getSimpleName();

    final Name id = getClassId(annotationDoc);

    panelStart("panel-primary", HtmlElem.H3, id, "annotation", name, null);

    description(annotationDoc, annotationDoc);

    final List<ExecutableElement> elements = getMethods(annotationDoc);
    if (elements.size() > 0) {

      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive parameters");
      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");
      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "Name");
      this.writer.element(HtmlElem.TH, "Type");
      this.writer.element(HtmlElem.TH, "Default");
      this.writer.startTag(HtmlElem.TH);
      this.writer.attribute(HtmlAttr.CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlElem.TH);
      this.writer.endTagLn(HtmlElem.TR);
      this.writer.endTagLn(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final ExecutableElement element : elements) {
        this.writer.startTag(HtmlElem.TR);
        final Name elementName = element.getSimpleName();

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "name");
        anchor(id + "." + elementName, elementName);
        this.writer.endTagLn(HtmlElem.TD);

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "type");
        typeNameLink(element.getReturnType());
        this.writer.endTagLn(HtmlElem.TD);

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "default");
        final AnnotationValue defaultValue = element.getDefaultValue();
        if (defaultValue == null) {
          this.writer.text("-");
        } else {
          this.writer.text(defaultValue);
        }
        this.writer.endTagLn(HtmlElem.TD);

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "description");
        description(null, element);
        this.writer.endTagLn(HtmlElem.TD);
        this.writer.endTagLn(HtmlElem.TR);
      }
      this.writer.endTagLn(HtmlElem.TBODY);

      this.writer.endTagLn(HtmlElem.TABLE);
      this.writer.endTagLn(HtmlElem.DIV);

    }
    panelEnd();
  }

  public void documentationAnnotations(final PackageElement PackageElement) {
    for (final TypeElement annotationDoc : getAnnotations(PackageElement)) {
      documentationAnnotation(annotationDoc);
    }
  }

  public void documentationClass(final String classType, final TypeElement element) {
    final Name id = getClassId(element);
    final Name name = element.getSimpleName();

    panelStart("panel-primary", HtmlElem.H3, id, classType, name, null);

    description(element, element);

    final List<ExecutableElement> constructors = getConstructors(element);
    if (constructors.size() > 0) {
      for (final ExecutableElement method : constructors) {
        documentationMethod(method);
      }
    }

    for (final ExecutableElement method : getMethods(element)) {
      documentationMethod(method);
    }
    panelEnd();
  }

  public void documentationClasses(final PackageElement packageElement) {
    for (final TypeElement TypeElement : getClasses(packageElement)) {
      documentationClass("class", TypeElement);
    }
  }

  public void documentationEnum(final TypeElement enumDoc) {
    final Name id = getClassId(enumDoc);
    final Name name = enumDoc.getSimpleName();

    panelStart("panel-primary", HtmlElem.H3, id, "enum", name, null);
    description(enumDoc, enumDoc);

    final List<VariableElement> elements = ElementFilter.fieldsIn(enumDoc.getEnclosedElements());
    if (elements.size() > 0) {
      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive parameters");
      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered");
      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "Constant");
      this.writer.startTag(HtmlElem.TH);
      this.writer.attribute(HtmlAttr.CLASS, "description");
      this.writer.text("Description");
      this.writer.endTag(HtmlElem.TH);
      this.writer.endTagLn(HtmlElem.TR);
      this.writer.endTagLn(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final VariableElement element : elements) {
        this.writer.startTag(HtmlElem.TR);
        final Name elementName = element.getSimpleName();

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "constant");
        this.writer.attribute(HtmlAttr.ID, id + "_" + elementName);
        HtmlUtil.elementWithId(this.writer, HtmlElem.SPAN, id + "." + elementName,
          elementName.toString());
        this.writer.endTagLn(HtmlElem.TD);

        this.writer.startTag(HtmlElem.TD);
        this.writer.attribute(HtmlAttr.CLASS, "description");
        description(null, element);
        this.writer.endTagLn(HtmlElem.TD);
        this.writer.endTagLn(HtmlElem.TR);
      }
      this.writer.endTagLn(HtmlElem.TBODY);

      this.writer.endTagLn(HtmlElem.TABLE);
      this.writer.endTagLn(HtmlElem.DIV);

    }
    panelEnd();
  }

  public void documentationEnums(final PackageElement packageElement) {
    for (final TypeElement enumDoc : getEnums(packageElement)) {
      documentationEnum(enumDoc);
    }
  }

  public void documentationInterfaces(final PackageElement PackageElement) {
    for (final TypeElement TypeElement : getInterfaces(PackageElement)) {
      documentationClass("interface", TypeElement);
    }
  }

  public void documentationMethod(final ExecutableElement member) {
    final String id = getMemberId(member);
    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "panel panel-info");

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "panel-heading");

    final String simpleId = id.replaceAll("[^a-zA-Z0-9_]", "_");
    this.writer.startTag(HtmlElem.A);
    this.writer.attribute(HtmlAttr.ID, id);
    this.writer.text("");
    this.writer.endTag(HtmlElem.A);

    this.writer.startTag(HtmlElem.H4);
    this.writer.attribute(HtmlAttr.CLASS, "panel-title");
    this.writer.attribute(HtmlAttr.ID, simpleId);
    methodSignature(member);
    this.writer.endTagLn(HtmlElem.H4);

    this.writer.endTagLn(HtmlElem.DIV);

    this.writer.startTag(HtmlElem.DIV);
    this.writer.attribute(HtmlAttr.CLASS, "panel-body");

    description((TypeElement)member.getEnclosingElement(), member);

    parameters(member);

    if (member instanceof ExecutableElement) {
      final ExecutableElement method = member;
      documentationReturn(method);
    }

    panelEnd();
  }

  public void documentationPackage(final PackageElement PackageElement) {
    final Name name = PackageElement.getSimpleName();
    final String id = name.toString();
    panelStart("panel-default", HtmlElem.H2, id, "package", name, null);

    description(null, PackageElement);

    documentationAnnotations(PackageElement);
    documentationEnums(PackageElement);
    documentationInterfaces(PackageElement);
    documentationClasses(PackageElement);

    panelEnd();
  }

  private String getAnchor(final ExecutableElement member) {
    final StringBuilder anchor = new StringBuilder();
    final TypeElement typeElement = (TypeElement)member.getEnclosingElement();
    final Name className = getClassId(typeElement);
    anchor.append(className);
    anchor.append(".");
    anchor.append(member.getSimpleName());
    anchor.append("(");
    final List<? extends VariableElement> parameters = member.getParameters();
    boolean first = true;
    for (final VariableElement parameter : parameters) {
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
      this.writer.startTag(HtmlElem.SMALL);
      final ExecutableElement method = member;
      final TypeMirror returnType = method.getReturnType();
      typeName(returnType);
      this.writer.text(" ");
      this.writer.endTagLn(HtmlElem.SMALL);
    }
    if (isStatic(member)) {
      this.writer.startTag(HtmlElem.I);
    }
    this.writer.text(member.getSimpleName());
    if (isStatic(member)) {
      this.writer.endTag(HtmlElem.I);
    }
    this.writer.startTag(HtmlElem.SMALL);
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
    this.writer.endTagLn(HtmlElem.SMALL);
    this.writer.endTagLn(HtmlElem.A);
  }

  @Override
  public void navbar() {
    navbarStart(this.docTitle);
    for (final PackageElement packageElement : getPackages()) {
      navMenus(getAnnotations(packageElement));
      navMenus(getEnums(packageElement));
      navMenus(getInterfaces(packageElement));
      navMenus(getClasses(packageElement));
    }
    navbarEnd();
  }

  public void navMenu(final TypeElement element) {
    final Name name = element.getSimpleName();
    final Name id = getClassId(element);
    navDropdownStart(name, "#" + id, false);
    for (final ExecutableElement ExecutableElement : getMethods(element)) {
      navMenu(element, ExecutableElement);
    }
    navDropdownEnd();
  }

  public void navMenu(final TypeElement TypeElement, final ExecutableElement element) {
    final Name name = element.getSimpleName();
    final String id = getMemberId(element);
    navMenuItem(name, "#" + id);
  }

  protected void navMenus(final Collection<? extends TypeElement> elements) {
    for (final TypeElement element : elements) {
      navMenu(element);
    }
  }

  protected void navMenus(final Map<Name, ? extends TypeElement> classes) {
    for (final TypeElement TypeElement : classes.values()) {
      navMenu(TypeElement);
    }
  }

  private void parameters(final ExecutableElement method) {
    final List<VariableElement> parameters = new ArrayList<>();
    for (final VariableElement parameter : method.getParameters()) {
      parameters.add(parameter);
    }
    if (!parameters.isEmpty()) {
      final Map<String, DocTree[]> descriptions = getVariableElementDescriptions(method);

      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive parameters");
      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");
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

        final TypeMirror type = parameter.asType();
        typeNameLink(type);
        this.writer.endTagLn(HtmlElem.TD);

        // descriptionTd(method.getEnclosingElement(), descriptions, name);
        this.writer.endTagLn(HtmlElem.TR);
      }
      this.writer.endTagLn(HtmlElem.TBODY);

      this.writer.endTagLn(HtmlElem.TABLE);
      this.writer.endTagLn(HtmlElem.DIV);
    }
  }
}
