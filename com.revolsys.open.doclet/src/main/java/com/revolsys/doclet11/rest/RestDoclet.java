package com.revolsys.doclet11.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import com.revolsys.collection.set.Sets;
import com.revolsys.doclet11.BaseDoclet;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class RestDoclet extends BaseDoclet {

  private static Set<String> PARAMETER_IGNORE_CLASS_NAMES = Sets
    .newHash("javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");

  public RestDoclet() {
    super("REST");
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
    for (final PackageElement packageElement : getPackages()) {
      for (final TypeElement element : getClasses(packageElement)) {
        documentationClass(element);
      }
    }
    endContentContainer();
  }

  public void documentationClass(final TypeElement element) {
    if (hasAnnotation(element, "org.springframework.stereotype.Controller")) {
      final Name id = getClassId(element);
      final Name name = element.getSimpleName();
      final String title = CaseConverter.toCapitalizedWords(name.toString());
      panelStart("panel-default", HtmlElem.H2, id, null, title, null);
      description(element, element);
      final List<ExecutableElement> methods = getMethods(element);
      for (final ExecutableElement ExecutableElement : methods) {
        documentationMethod(element, ExecutableElement);
      }
      panelEnd();
    }
  }

  public void documentationMethod(final Element TypeElement, final ExecutableElement element) {
    final AnnotationMirror requestMapping = getAnnotation(element,
      "com.revolsys.ui.web.annotation.RequestMapping");
    if (requestMapping != null) {
      final String id = getMethodId(element);
      final Name methodName = element.getSimpleName();
      final String title = CaseConverter.toCapitalizedWords(methodName.toString());
      panelStart("panel-primary", HtmlElem.H3, id, null, title, null);

      description((TypeElement)element.getEnclosingElement(), element);
      requestMethods(requestMapping);
      uriTemplates(requestMapping);
      uriTemplateVariableElements(element);
      parameters(element);
      responseStatus(element);

      panelEnd();
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getElementValue(final AnnotationMirror annotation, final String name) {
    for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> pair : annotation
      .getElementValues()
      .entrySet()) {
      if (pair.getKey().getSimpleName().toString().equals(name)) {
        return (T)pair.getValue().getValue();
      }
    }
    return null;
  }

  protected String getMethodId(final ExecutableElement member) {
    final TypeElement element = (TypeElement)member.getEnclosingElement();
    final Name methodName = member.getSimpleName();
    final Name classId = getClassId(element);
    return classId + "." + methodName;
  }

  @Override
  public void navbar() {
    navbarStart(this.docTitle);
    for (final PackageElement packageElement : getPackages()) {
      for (final TypeElement TypeElement : getClasses(packageElement)) {
        navMenu(TypeElement);
      }
    }
    navbarEnd();
  }

  public void navMenu(final Element TypeElement, final ExecutableElement element) {
    final Name name = element.getSimpleName();
    final String id = getMethodId(element);
    final String title = CaseConverter.toCapitalizedWords(name.toString());
    navMenuItem(title, "#" + id);
  }

  public void navMenu(final TypeElement element) {
    final Name id = getClassId(element);
    final Name name = element.getSimpleName();
    final String title = CaseConverter.toCapitalizedWords(name.toString());
    navDropdownStart(title, "#" + id, false);
    for (final ExecutableElement ExecutableElement : ElementFilter
      .methodsIn(element.getEnclosedElements())) {
      final AnnotationMirror requestMapping = getAnnotation(ExecutableElement,
        "com.revolsys.ui.web.annotation.RequestMapping");
      if (requestMapping != null) {
        navMenu(element, ExecutableElement);
      }
    }
    navDropdownEnd();
  }

  private void parameters(final ExecutableElement method) {
    final List<VariableElement> parameters = new ArrayList<>();
    // for (final VariableElement parameter : method.getParameters()) {
    // final List<? extends AnnotationMirror> annotations =
    // parameter.getAnnotationMirrors();
    // if (hasAnnotation(annotations,
    // "org.springframework.web.bind.annotation.RequestParam")
    // || hasAnnotation(annotations,
    // "org.springframework.web.bind.annotation.RequestBody")) {
    // parameters.add(parameter);
    // }
    // }
    // if (!parameters.isEmpty()) {
    // final Map<String, Tag[]> descriptions =
    // getVariableElementDescriptions(method);
    //
    // panelStart("panel-info", HtmlElem.H4, null, null, "VariableElements",
    // null);
    // this.writer.element(HtmlElem.P, "The resource supports the following
    // parameters. "
    // + "For HTTP get requests these must be specified using query string
    // parameters. "
    // + "For HTTP POST requests these can be specified using query string,
    // application/x-www-form-urlencoded parameters or multipart/form-data
    // unless otherwise specified. "
    // + "Array values [] can be specified by including the parameter multiple
    // times in the request.");
    //
    // this.writer.startTag(HtmlElem.DIV);
    // this.writer.attribute(HtmlAttr.CLASS, "table-responsive");
    // this.writer.startTag(HtmlElem.TABLE);
    // this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered
    // table-condensed");
    //
    // this.writer.startTag(HtmlElem.THEAD);
    // this.writer.startTag(HtmlElem.TR);
    // this.writer.element(HtmlElem.TH, "VariableElement");
    // this.writer.element(HtmlElem.TH, "Type");
    // this.writer.element(HtmlElem.TH, "Default");
    // this.writer.element(HtmlElem.TH, "Required");
    // this.writer.element(HtmlElem.TH, "Description");
    // this.writer.endTag(HtmlElem.TR);
    // this.writer.endTag(HtmlElem.THEAD);
    //
    // this.writer.startTag(HtmlElem.TBODY);
    // for (final VariableElement parameter : parameters) {
    // String typeName = getTypeName(parameter).toString();
    // if (PARAMETER_IGNORE_CLASS_NAMES.contains(typeName)) {
    // typeName = typeName.replaceAll("java.util.List<([^>]+)>", "$1\\[\\]");
    // typeName = typeName.replaceFirst("^java.lang.", "");
    // typeName =
    // typeName.replaceAll("org.springframework.web.multipart.MultipartFile",
    // "File");
    // this.writer.startTag(HtmlElem.TR);
    // final Name name = parameter.getSimpleName();
    // final AnnotationMirror requestParam =
    // getAnnotation(parameter.getAnnotationMirrors(),
    // "org.springframework.web.bind.annotation.RequestParam");
    // final AnnotationMirror requestBody =
    // getAnnotation(parameter.getAnnotationMirrors(),
    // "org.springframework.web.bind.annotation.RequestBody");
    // String paramName = name.toString();
    // String defaultValue = "-";
    //
    // boolean required = true;
    // if (requestParam != null) {
    // final String value = getElementValue(requestParam, "value");
    // if (value != null && !value.trim().equals("")) {
    // paramName = value;
    // }
    // defaultValue = getElementValue(requestParam, "defaultValue");
    // if (defaultValue == null) {
    // defaultValue = "-";
    // }
    // required = Boolean.FALSE != (Boolean)getElementValue(requestParam,
    // "required");
    // }
    // if (requestBody != null) {
    // required = true;
    // paramName = "HTTP Request body or 'body' parameter";
    // typeName = "binary/character data";
    // }
    //
    // this.writer.startTag(HtmlElem.TD);
    // this.writer.startTag(HtmlElem.CODE);
    // this.writer.text(paramName);
    // this.writer.endTag(HtmlElem.CODE);
    // this.writer.endTag(HtmlElem.TD);
    //
    // this.writer.startTag(HtmlElem.TD);
    // this.writer.startTag(HtmlElem.CODE);
    // this.writer.text(typeName);
    // this.writer.endTag(HtmlElem.CODE);
    // this.writer.endTag(HtmlElem.TD);
    //
    // this.writer.element(HtmlElem.TD, defaultValue);
    // if (required) {
    // this.writer.element(HtmlElem.TD, "Yes");
    // } else {
    // this.writer.element(HtmlElem.TD, "No");
    // }
    // descriptionTd(method.getEnclosingElement(), descriptions, name);
    // this.writer.endTag(HtmlElem.TR);
    // }
    // }
    // this.writer.endTag(HtmlElem.TBODY);
    //
    // this.writer.endTag(HtmlElem.TABLE);
    // this.writer.endTag(HtmlElem.DIV);
    // panelEnd();
    // }
  }

  private void requestMethods(final AnnotationMirror requestMapping) {
    final AnnotationValue[] methods = getElementValue(requestMapping, "method");
    if (methods != null && methods.length > 0) {
      panelStart("panel-info", HtmlElem.H4, null, null, "HTTP Request Methods", null);
      this.writer.element(HtmlElem.P,
        "The resource can be accessed using the following HTTP request methods.");
      this.writer.startTag(HtmlElem.UL);
      for (final AnnotationValue value : methods) {
        final VariableElement method = (VariableElement)value.getValue();
        this.writer.element(HtmlElem.LI, method.getSimpleName());
      }
      this.writer.endTag(HtmlElem.UL);
      panelEnd();
    }
  }

  private void responseStatus(final ExecutableElement method) {
    final Map<String, List<String>> responseStatusDescriptions = new TreeMap<>();

    // for (final Tag tag : method.tags()) {
    // if (tag.getSimpleName().equals("@web.response.status")) {
    // final String text = description(method.getEnclosingElement(), tag);
    //
    // final int index = text.indexOf(" ");
    // if (index != -1) {
    // final String status = text.substring(0, index);
    // final String description = text.substring(index + 1).trim();
    // addResponseStatusDescription(responseStatusDescriptions, status,
    // description);
    // }
    // }
    // }
    addResponseStatusDescription(responseStatusDescriptions, "500",
      "<p><b>Internal Server Error</b></p>"
        + "<p>This error indicates that there was an unexpected error on the server. "
        + "This is sometimes temporary so try again after a few minutes. "
        + "The problem could also be caused by bad input data so verify all input parameters and files. "
        + "If the problem persists contact the support desk with exact details of the parameters you were using.</p>");
    if (!responseStatusDescriptions.isEmpty()) {
      panelStart("panel-info", HtmlElem.H4, null, null, "HTTP Status Codes", null);
      this.writer.element(HtmlElem.P,
        "The resource will return one of the following status codes. The HTML error page may include an error message. The descriptions of the messages and the cause are described below.");
      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive");

      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");

      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "HTTP Status Code");
      this.writer.element(HtmlElem.TH, "Description");
      this.writer.endTag(HtmlElem.TR);
      this.writer.endTag(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final Entry<String, List<String>> entry : responseStatusDescriptions.entrySet()) {
        final String code = entry.getKey();
        for (final String message : entry.getValue()) {
          this.writer.startTag(HtmlElem.TR);
          this.writer.element(HtmlElem.TD, code);
          this.writer.startTag(HtmlElem.TD);
          this.writer.write(message);
          this.writer.endTag(HtmlElem.TD);

          this.writer.endTag(HtmlElem.TR);
        }
      }
      this.writer.endTag(HtmlElem.TBODY);

      this.writer.endTag(HtmlElem.TABLE);
      this.writer.endTag(HtmlElem.DIV);
      panelEnd();
    }
  }

  private void uriTemplates(final AnnotationMirror requestMapping) {
    final AnnotationValue[] uriTemplates = getElementValue(requestMapping, "value");
    if (uriTemplates.length > 0) {
      panelStart("panel-info", HtmlElem.H4, null, null, "URI Templates", null);
      this.writer.element(HtmlElem.P,
        "The URI templates define the paths that can be appended to the base URL of the service to access this resource.");

      for (final AnnotationValue uriTemplate : uriTemplates) {
        this.writer.element(HtmlElem.PRE, uriTemplate.getValue());
      }
      panelEnd();
    }
  }

  private void uriTemplateVariableElements(final ExecutableElement method) {
    final List<VariableElement> parameters = new ArrayList<>();
    for (final VariableElement parameter : method.getParameters()) {
      if (hasAnnotation(parameter.getAnnotationMirrors(),
        "org.springframework.web.bind.annotation.PathVariable")) {
        parameters.add(parameter);
      }
    }
    // if (!parameters.isEmpty()) {
    // final Map<String, Tag[]> descriptions =
    // getVariableElementDescriptions(method);
    // panelStart("panel-info", HtmlElem.H4, null, null, "URI Template
    // VariableElements", null);
    // this.writer.element(HtmlElem.P,
    // "The URI templates support the following parameters which must be
    // replaced with values as described below.");
    // this.writer.startTag(HtmlElem.DIV);
    // this.writer.attribute(HtmlAttr.CLASS, "table-responsive");
    //
    // this.writer.startTag(HtmlElem.TABLE);
    // this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered
    // table-condensed");
    //
    // this.writer.startTag(HtmlElem.THEAD);
    // this.writer.startTag(HtmlElem.TR);
    // this.writer.element(HtmlElem.TH, "VariableElement");
    // this.writer.element(HtmlElem.TH, "Type");
    // this.writer.element(HtmlElem.TH, "Description");
    // this.writer.endTag(HtmlElem.TR);
    // this.writer.endTag(HtmlElem.THEAD);
    //
    // this.writer.startTag(HtmlElem.TBODY);
    // for (final VariableElement parameter : parameters) {
    // this.writer.startTag(HtmlElem.TR);
    // final Name name = parameter.getSimpleName();
    // this.writer.element(HtmlElem.TD, "{" + name + "}");
    // this.writer.element(HtmlElem.TD, getTypeName(parameter));
    //// descriptionTd(method.getEnclosingElement(), descriptions, name);
    //
    // this.writer.endTag(HtmlElem.TR);
    // }
    // this.writer.endTag(HtmlElem.TBODY);
    //
    // this.writer.endTag(HtmlElem.TABLE);
    // this.writer.endTag(HtmlElem.DIV);
    // panelEnd();
    // }
  }

}
