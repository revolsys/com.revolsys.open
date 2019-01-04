package com.revolsys.doclet11;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.xml.namespace.QName;

import com.revolsys.doclet11.option.ConsumerOption;
import com.revolsys.doclet11.option.FunctionOption;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.SeeTree;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

public class BaseDoclet implements Doclet {
  protected String destDir = ".";

  protected String docTitle;

  protected Set<String> customCssUrls = new LinkedHashSet<>();

  protected DocletEnvironment environment;

  protected XmlWriter writer;

  protected Locale locale;

  protected Reporter reporter;

  protected Set<Option> options = new LinkedHashSet<>();

  private final String name;

  private final Map<String, String> PACKAGE_URLS = new LinkedHashMap<>();

  {
    addPackageUrl("java.", "http://docs.oracle.com/javase/8/docs/api/");
    addPackageUrl("com.revolsys.jts.", "http://tsusiatsoftware.net/jts/javadoc/");
  }

  public BaseDoclet(final String name) {
    this.name = name;
    this.options.add(new FunctionOption("-d", "Destination directory", "file", this::setDestDir));
    this.options.add(
      new ConsumerOption("-doctitle", "RetainJavaDocComment Title", "title", this::setDocTitle));
    this.options
      .add(new ConsumerOption("-customcssurl", "Add Custom CSS URL", "url", this::addCustomCssUrl));

  }

  public void addCustomCssUrl(final String cssUrl) {
    this.customCssUrls.add(cssUrl);
  }

  public void addPackageUrl(final String packagePrefix, final String url) {
    this.PACKAGE_URLS.put(packagePrefix, url);
  }

  public void anchor(final CharSequence name, final CharSequence title) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.A);
    writer.attribute(HtmlAttr.NAME, name);
    writer.text(title);
    writer.endTag(HtmlElem.A);
  }

  public void contentContainer(final String firstColClass) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "container-fluid");

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "row");

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, firstColClass);
  }

  public void copyFiles(final String destDir) {
    for (final String name : Arrays.asList("bootstrap-custom.css", "javadoc.css", "javadoc.js",
      "javadoc.js")) {
      FileUtil.copy(getClass().getResourceAsStream("/com/revolsys/doclet/" + name),
        new File(destDir, name));
    }
  }

  public void description(final TypeElement containingClass, final DocCommentTree tags) {
    final XmlWriter writer = this.writer;

    // if (tags != null && tags.length > 0) {
    // for (final DocTree tag : tags) {
    // final DocTree.Kind kind = tag.getKind();
    // if (tag instanceof SeeTree) {
    // final SeeTree seeTag = (SeeTree)tag;
    // seeTag(containingClass, seeTag);
    // } else if (kind == DocTree.Kind.TEXT) {
    // writer.write(tag.getText());
    // }
    // }
    // }
  }

  public String description(final TypeElement containingClass, final DocTree doc) {
    // final DocTree[] tags = doc.inlineTags();
    // return descriptionString(containingClass, tags);
    return null;
  }

  public void description(final TypeElement containingClass, final Element element) {
    final XmlWriter writer = this.writer;
    final DocCommentTree tags = getDocTree(element);
    description(containingClass, tags);
  }

  public String descriptionString(final TypeElement containingClass, final DocTree[] tags) {
    final StringBuilder text = new StringBuilder();
    // if (tags != null && tags.length > 0) {
    // for (final DocTree tag : tags) {
    // final DocTree.Kind kind = tag.getKind();
    // if (tag instanceof SeeTree) {
    // final SeeTree seeTag = (SeeTree)tag;
    // seeTag(text, containingClass, seeTag);
    // } else if (kind == DocTree.Kind.TEXT) {
    // text.append(tag.getText());
    // }
    // }
    // }
    return text.toString();
  }

  public void descriptionTd(final TypeElement containingClass,
    final Map<String, DocTree[]> descriptions, final String name) {
    // final XmlWriter writer = this.writer;
    // writer.startTag(HtmlElem.TD);
    // writer.attribute(HtmlAttr.CLASS, "description");
    // final DocTree[] description = descriptions.get(name);
    // description(containingClass, description);
    // writer.endTagLn(HtmlElem.TD);
  }

  public void documentation() {
  }

  public void documentationReturn(final ExecutableElement method) {
    // final XmlWriter writer = this.writer;
    // final TypeMirror type = method.getReturnType();
    // if (type != null && !"void".equals(getQualifiedName(type).toString())) {
    // DocTree[] descriptionTags = null;
    // for (final Tag tag : method.tags()) {
    // if (tag.getSimpleName().equals("@return")) {
    // descriptionTags = tag.inlineTags();
    // }
    // }
    // writer.startTag(HtmlElem.DIV);
    // writer.startTag(HtmlElem.STRONG);
    // writer.text("Return");
    // writer.endTag(HtmlElem.STRONG);
    // writer.endTagLn(HtmlElem.DIV);
    //
    // typeNameLink(type);
    // writer.text(" ");
    // description((TypeElement)method.getEnclosingElement(), descriptionTags);
    // }
  }

  public void endContentContainer() {
    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
    this.writer.endTagLn(HtmlElem.DIV);
  }

  public AnnotationMirror getAnnotation(final Element doc, final String name) {
    final List<? extends AnnotationMirror> annotations = doc.getAnnotationMirrors();
    return getAnnotation(annotations, name);
  }

  public AnnotationMirror getAnnotation(final List<? extends AnnotationMirror> annotations,
    final String name) {
    for (final AnnotationMirror annotation : annotations) {
      final DeclaredType annotationType = annotation.getAnnotationType();
      final String annotationName = qualifiedName(annotationType).toString();
      if (name.equals(annotationName)) {
        return annotation;
      }
    }
    return null;
  }

  public List<TypeElement> getAnnotations(final PackageElement packageElement) {
    return getElements(packageElement, ElementKind.ANNOTATION_TYPE);
  }

  protected Collection<TypeElement> getClasses(final PackageElement packageElement) {
    return getElements(packageElement, ElementKind.CLASS);
  }

  protected Name getClassId(final TypeElement TypeElement) {
    return qualifiedName(TypeElement);
  }

  protected List<ExecutableElement> getConstructors(final TypeElement element) {
    return ElementFilter.constructorsIn(element.getEnclosedElements());
  }

  public String getDimension(final TypeMirror type) {
    final StringBuilder dimension = new StringBuilder();
    for (TypeMirror t = type; t.getKind() == TypeKind.ARRAY; t = ((ArrayType)t)
      .getComponentType()) {
      dimension.append("[]");
    }
    return dimension.toString();
  }

  protected DocCommentTree getDocTree(final Element element) {
    return this.environment.getDocTrees().getDocCommentTree(element);
  }

  public List<TypeElement> getElements(final PackageElement packageElement,
    final ElementKind kind) {
    final Map<Name, TypeElement> elements = new TreeMap<>();
    for (final Element element : packageElement.getEnclosedElements()) {
      if (element.getKind() == kind) {
        final Name name = element.getSimpleName();
        elements.put(name, (TypeElement)element);
      }
    }
    return new ArrayList<>(elements.values());
  }

  public List<TypeElement> getEnums(final PackageElement packageElement) {
    return getElements(packageElement, ElementKind.ENUM);
  }

  public String getExternalUrl(final CharSequence qualifiedTypeName) {
    final String nameString = qualifiedTypeName.toString();
    for (final Entry<String, String> entry : this.PACKAGE_URLS.entrySet()) {
      final String packagePrefix = entry.getKey();
      if (nameString.startsWith(packagePrefix)) {
        final String baseUrl = entry.getValue();
        final String url = baseUrl + nameString.replaceAll("\\.", "/") + ".html?is-external=true";
        return url;
      }
    }
    return null;
  }

  public List<TypeElement> getInterfaces(final PackageElement packageElement) {
    return getElements(packageElement, ElementKind.INTERFACE);
  }

  protected String getMemberId(final ExecutableElement member) {
    final StringBuilder id = new StringBuilder();
    final TypeElement TypeElement = (TypeElement)member.getEnclosingElement();
    final Name classId = getClassId(TypeElement);
    id.append(classId);
    id.append(".");
    final Name memberName = member.getSimpleName();
    id.append(memberName);
    for (final VariableElement parameter : member.getParameters()) {
      id.append("-");
      final TypeMirror type = parameter.asType();
      String typeName = getQualifiedName(type).toString();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      id.append(typeName);
      id.append(getDimension(type));
    }
    return id.toString().replaceAll("[^A-Za-z0-9\\-_.]", "_");
  }

  protected List<ExecutableElement> getMethods(final TypeElement element) {
    return ElementFilter.methodsIn(element.getEnclosedElements());
  }

  @Override
  public String getName() {
    return this.name;
  }

  public Name getName(final TypeMirror type) {
    final TypeElement element = getTypeElement(type);
    return element.getSimpleName();
  }

  protected Set<PackageElement> getPackages() {
    return ElementFilter.packagesIn(this.environment.getSpecifiedElements());
  }

  public Name getQualifiedName(final TypeMirror type) {
    final TypeElement element = getTypeElement(type);
    return element.getQualifiedName();
  }

  @Override
  public Set<? extends Option> getSupportedOptions() {
    return this.options;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_11;
  }

  protected TypeElement getTypeElement(final TypeMirror type) {
    return (TypeElement)this.environment.getTypeUtils().asElement(type);
  }

  public Name getTypeName(final VariableElement element) {
    final TypeMirror type = element.asType();
    return getName(type);
  }

  public Map<String, DocTree[]> getVariableElementDescriptions(final ExecutableElement method) {
    final Map<String, DocTree[]> descriptions = new HashMap<>();
    // for (final ParamTree tag : method.ParamTrees()) {
    // final String parameterName = tag.parameterName();
    // final DocTree[] commentTags = tag.inlineTags();
    // descriptions.put(parameterName, commentTags);
    // }
    return descriptions;
  }

  public boolean hasAnnotation(final Element doc, final String name) {
    final AnnotationMirror annotation = getAnnotation(doc, name);
    return annotation != null;
  }

  public boolean hasAnnotation(final List<? extends AnnotationMirror> annotations,
    final String name) {
    final AnnotationMirror annotation = getAnnotation(annotations, name);
    return annotation != null;
  }

  public void headOld(final String docTitle) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.HEAD);
    writer.element(HtmlElem.TITLE, docTitle);
    for (final String url : Arrays.asList(
      "https://code.jquery.com/ui/1.11.2/themes/cupertino/jquery-ui.css",
      "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/prettify.min.css",
      "https://cdn.datatables.net/1.10.6/css/jquery.dataTables.min.css", "javadoc.css")) {
      HtmlUtil.serializeCss(writer, url);

    }
    for (final String url : Arrays.asList("https://code.jquery.com/jquery-1.12.1.min.js",
      "https://code.jquery.com/ui/1.11.4/jquery-ui.min.js",
      "https://cdn.datatables.net/1.10.11/js/jquery.dataTables.min.js", "javadoc.js")) {
      HtmlUtil.serializeScriptLink(writer, url);
    }
    writer.endTagLn(HtmlElem.HEAD);
  }

  public void htmlFoot() {
    final XmlWriter writer = this.writer;
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js",
      "https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js",
      "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js",
      "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/prettify.min.js");
    writer.startTag(HtmlElem.SCRIPT);
    writer.textLn("$(function() {");
    writer.textLn("  prettyPrint();");
    writer.textLn("});");
    writer.endTag(HtmlElem.SCRIPT);

    writer.endTagLn(HtmlElem.BODY);
    writer.endTagLn(HtmlElem.HTML);
    writer.endDocument();
  }

  public void htmlHead(final String docTitle, final Collection<String> customCssUrls) {
    final XmlWriter writer = this.writer;
    writer.docType("<!DOCTYPE html>");
    writer.startTag(HtmlElem.HTML);
    writer.attribute(HtmlAttr.LANG, "en");
    writer.newLine();

    writer.startTagLn(HtmlElem.HEAD);

    writer.startTag(HtmlElem.META);
    writer.attribute(HtmlAttr.CHARSET, "utf-8");
    writer.endTagLn(HtmlElem.META);

    writer.startTag(HtmlElem.META);
    writer.attribute(HtmlAttr.HTTP_EQUIV, "X-UA-Compatible");
    writer.attribute(HtmlAttr.CONTENT, "IE=edge");
    writer.endTagLn(HtmlElem.META);

    writer.startTag(HtmlElem.META);
    writer.attribute(HtmlAttr.NAME, "viewport");
    writer.attribute(HtmlAttr.CONTENT, "width=device-width, initial-scale=1");
    writer.endTagLn(HtmlElem.META);

    writer.elementLn(HtmlElem.TITLE, docTitle);

    HtmlUtil.serializeCss(writer,
      "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css",
      "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/prettify.min.css",
      "bootstrap-custom.css");
    if (Property.hasValue(customCssUrls)) {
      HtmlUtil.serializeCss(writer, customCssUrls);
    }
    HtmlUtil.serializeStyle(writer, "body{padding-top:60px}\n"
      + "*[id]:before {display:block;content:' ';margin-top:-75px;height:75px;visibility:hidden;}");
    writer.endTagLn(HtmlElem.HEAD);

    writer.startTag(HtmlElem.BODY);
    writer.attribute("data-spy", "scroll");
    writer.attribute("data-target", "#navMain");
    writer.attribute("data-offset", "60");
    writer.newLine();
  }

  @Override
  public void init(final Locale locale, final Reporter reporter) {
    this.locale = locale;
    this.reporter = reporter;
  }

  public boolean isIncluded(final Element element) {
    return this.environment.isIncluded(element);
  }

  public boolean isStatic(final Element e) {
    return e.getModifiers().contains(Modifier.STATIC);
  }

  public boolean isTypeIncluded(final TypeMirror type) {
    final TypeElement typeElement = getTypeElement(type);
    return isIncluded(typeElement);
  }

  public void label(final String label, final boolean code) {
    final XmlWriter writer = this.writer;
    if (code) {
      writer.startTag(HtmlElem.CODE);
    }
    writer.text(label);
    if (code) {
      writer.endTagLn(HtmlElem.CODE);
    }
  }

  public void label(final StringBuilder text, final String label, final boolean code) {
    if (code) {
      text.append("<code>");
    }
    text(text, label);
    if (code) {
      text.append("</code>");
    }
  }

  public void link(final String url, final String label, final boolean code) {
    final XmlWriter writer = this.writer;
    final boolean hasUrl = Property.hasValue(url);
    if (hasUrl) {
      writer.startTag(HtmlElem.A);
      writer.attribute(HtmlAttr.HREF, url);
    }
    label(label, code);
    if (hasUrl) {
      writer.endTag(HtmlElem.A);
    }
  }

  public void link(final StringBuilder text, final String url, final String label,
    final boolean code) {
    final boolean hasUrl = Property.hasValue(url);
    if (hasUrl) {
      text.append("<a href=\"");
      text.append(url);
      text.append("\">");
    }
    label(text, label, code);
    if (hasUrl) {
      text.append("</a>");
    }
  }

  public void navbar() {
  }

  public void navbarEnd() {
    final XmlWriter writer = this.writer;
    writer.endTagLn(HtmlElem.UL);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.NAV);

  }

  public void navbarStart(final String title) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.NAV);
    writer.attribute(HtmlAttr.ID, "navMain");
    writer.attribute(HtmlAttr.CLASS, "navbar navbar-default navbar-fixed-top");
    writer.newLine();

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "container");
    writer.newLine();

    {
      writer.startTag(HtmlElem.DIV);
      writer.attribute(HtmlAttr.CLASS, "navbar-header");
      writer.newLine();
      {
        writer.startTag(HtmlElem.BUTTON);
        writer.attribute(HtmlAttr.TYPE, "button");
        writer.attribute(HtmlAttr.CLASS, "navbar-toggle collapsed");
        writer.attribute("data-toggle", "collapse");
        writer.attribute("data-target", "#navbar");
        writer.attribute("aria-expanded", "false");
        writer.attribute("aria-controls", "navbar");
        writer.newLine();

        HtmlUtil.serializeSpan(writer, "sr-only", "Toggle navigation");

        for (int i = 0; i < 3; i++) {
          writer.startTag(HtmlElem.SPAN);
          writer.attribute(HtmlAttr.CLASS, "icon-bar");
          writer.text("");
          writer.endTag(HtmlElem.SPAN);
        }
        writer.endTagLn(HtmlElem.BUTTON);
      }
      {
        writer.startTag(HtmlElem.DIV);
        writer.attribute(HtmlAttr.CLASS, "navbar-brand");
        writer.startTag(HtmlElem.A);
        writer.attribute(HtmlAttr.HREF, "#");
        HtmlUtil.serializeSpan(writer, "navbar-brand-title", title);
        writer.endTag(HtmlElem.A);
        writer.endTag(HtmlElem.DIV);
      }
      writer.endTagLn(HtmlElem.DIV);
    }
    {
      writer.startTag(HtmlElem.DIV);
      writer.attribute(HtmlAttr.ID, "navbar");
      writer.attribute(HtmlAttr.CLASS, "navbar-collapse collapse");
      writer.attribute("aria-expanded", "false");
      writer.newLine();

      writer.startTag(HtmlElem.UL);
      writer.attribute(HtmlAttr.CLASS, "nav navbar-nav");

    }
  }

  public void navDropdownEnd() {
    final XmlWriter writer = this.writer;
    writer.endTagLn(HtmlElem.UL);
    writer.endTagLn(HtmlElem.LI);
  }

  public void navDropdownStart(final CharSequence title, String url, final boolean subMenu) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.LI);
    if (subMenu) {
      writer.attribute(HtmlAttr.CLASS, "dropdown-submenu");
    } else {
      writer.attribute(HtmlAttr.CLASS, "dropdown");
    }

    writer.startTag(HtmlElem.A);
    if (url.startsWith("#")) {
      url = "#" + url.substring(1).replaceAll("[^a-zA-Z0-9_]", "_");
    }
    if (subMenu) {
      writer.attribute(HtmlAttr.HREF, url);
    } else {
      writer.attribute(HtmlAttr.HREF, "#");
      writer.attribute(HtmlAttr.CLASS, "dropdown-toggle");
      writer.attribute("data-toggle", "dropdown");
      writer.attribute(HtmlAttr.ROLE, "button");
      writer.attribute("aria-expanded", "false");
    }
    writer.text(title);
    if (!subMenu) {
      writer.startTag(HtmlElem.SPAN);
      writer.attribute(HtmlAttr.CLASS, "caret");
      writer.text("");
      writer.endTag(HtmlElem.SPAN);
    }
    writer.endTag(HtmlElem.A);

    writer.startTag(HtmlElem.UL);
    writer.attribute(HtmlAttr.CLASS, "dropdown-menu");
    writer.attribute(HtmlAttr.ROLE, "menu");
    writer.newLine();
    if (!subMenu) {
      navMenuItem(title, url);
      writer.startTag(HtmlElem.LI);
      writer.attribute(HtmlAttr.CLASS, "divider");
      writer.endTagLn(HtmlElem.LI);
    }
  }

  public void navMenuItem(final CharSequence title, String url) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.LI);

    writer.startTag(HtmlElem.A);
    if (url.startsWith("#")) {
      url = "#" + url.substring(1).replaceAll("[^a-zA-Z0-9_]", "_");
    }
    writer.attribute(HtmlAttr.HREF, url);
    writer.text(title);
    writer.endTag(HtmlElem.A);

    writer.endTagLn(HtmlElem.LI);
  }

  public int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d")) {
      return 2;
    } else if (optionName.equals("-doctitle")) {
      return 2;
    } else if (optionName.equals("-customcssurl")) {
      return 2;
    }
    return 0;
  }

  public void panelEnd() {
    final XmlWriter writer = this.writer;
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
  }

  public void panelStart(final String panelClass, final QName headerElement, final CharSequence id,
    final String titlePrefix, final CharSequence title, final String titleSuffix) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "panel " + panelClass);
    writer.newLine();

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "panel-heading");
    writer.newLine();

    String simpleId = null;
    if (Property.hasValue(id)) {
      simpleId = id.toString().replaceAll("[^a-zA-Z0-9_]", "_");
      if (!id.equals(simpleId)) {
        writer.startTag(HtmlElem.A);
        writer.attribute(HtmlAttr.ID, id);
        writer.text("");
        writer.endTag(HtmlElem.A);
      }
    }
    writer.startTag(headerElement);
    writer.attribute(HtmlAttr.CLASS, "panel-title");

    if (Property.hasValue(id)) {
      writer.attribute(HtmlAttr.ID, simpleId);
    }
    if (Property.hasValue(titlePrefix)) {
      writer.element(HtmlElem.SMALL, titlePrefix);
      writer.text(" ");
    }
    writer.text(title);
    if (Property.hasValue(titleSuffix)) {
      writer.text(" ");
      writer.element(HtmlElem.SMALL, titleSuffix);
    }
    writer.endTagLn(headerElement);

    writer.endTagLn(HtmlElem.DIV);

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "panel-body");
    writer.newLine();
  }

  protected void preRun() {
  }

  public Name qualifiedName(final TypeElement element) {
    return element.getQualifiedName();
  }

  public Name qualifiedName(final TypeMirror type) {
    final Types typeUtils = this.environment.getTypeUtils();
    final TypeElement element = (TypeElement)typeUtils.asElement(type);
    return qualifiedName(element);
  }

  public String replaceDocRootDir(final String text) {
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

  @Override
  public boolean run(final DocletEnvironment root) {
    this.environment = root;
    preRun();
    final File dir = new File(this.destDir);
    final File indexFile = new File(dir, "index.html");
    try (
      final FileWriter out = new FileWriter(indexFile)) {
      this.writer = new XmlWriter(out, false);
      this.writer.setIndent(false);
      this.writer.setWriteNewLine(false);
      copyFiles(this.destDir);

      htmlHead(this.docTitle, this.customCssUrls);

      navbar();

      documentation();

      htmlFoot();
    } catch (final IOException e) {
      throw new IllegalArgumentException(e.fillInStackTrace().getMessage(), e);
    }
    return true;
  }

  public void seeTag(final StringBuilder text, final TypeElement containingClass,
    final SeeTree seeTag) {
    // final String name = seeTag.getSimpleName();
    // if (name.startsWith("@link") || name.equals("@see")) {
    // final boolean code = !name.equalsIgnoreCase("@linkplain");
    // String label = seeTag.label();
    //
    // final StringBuffer stringbuffer = new StringBuffer();
    //
    // final String seeTagText = replaceDocRootDir(seeTag.text());
    // if (seeTagText.startsWith("<") || seeTagText.startsWith("\"")) {
    // stringbuffer.append(seeTagText);
    // text.append(seeTagText);
    // } else {
    // final TypeElement referencedClass = seeTag.referencedClass();
    // final Element referencedMember = seeTag.referencedMember();
    // String referencedMemberName = seeTag.referencedMemberName();
    // if (referencedClass == null) {
    // final PackageElement packageElement = seeTag.referencedPackage();
    // if (packageElement != null && isIncluded(packageElement)) {
    // final Name packageName = packageElement.getSimpleName();
    // if (!StringUtils.hasText(label)) {
    // label = packageName.toString();
    // }
    // link(text, "#" + packageName, label, code);
    // } else {
    // // TODO link to external package or class
    // // String s9 = getCrossPackageLink(referencedClassName);
    // // String s8;
    // // if (s9 != null)
    // // stringbuffer.append(getHyperLink(s9, "", s1.length() != 0 ? s1
    // // : s3, false));
    // // else if ((s8 = getCrossClassLink(referencedClassName,
    // // referencedMemberName, s1, false, "", !plainLink)) != null) {
    // // stringbuffer.append(s8);
    // // } else {
    // // configuration.getDocletSpecificMsg().warning(seeTag.position(),
    // // "doclet.see.class_or_package_not_found", name, s2);
    // // stringbuffer.append(s1.length() != 0 ? s1 : s3);
    // // }
    // }
    // } else {
    // String url = null;
    // final Name className = referencedClass.getQualifiedName();
    // if (isIncluded(referencedClass)) {
    // url = "#" + className;
    // } else {
    // url = getExternalUrl(className);
    // if (!StringUtils.hasText(url)) {
    // label = className.toString();
    // }
    // }
    // if (referencedMember != null) {
    // if (referencedMember instanceof ExecutableElement) {
    // if (referencedMemberName.indexOf('(') < 0) {
    // final ExecutableElement executableDoc =
    // (ExecutableElement)referencedMember;
    // referencedMemberName = referencedMemberName + executableDoc.signature();
    // }
    // if (StringUtils.hasText(referencedMemberName)) {
    // label = referencedMemberName;
    // } else {
    // label = seeTagText;
    // }
    // }
    // if (isIncluded(referencedClass)) {
    // url += "." + referencedMemberName;
    // } else if (StringUtils.hasText(url)) {
    // url += "#" + referencedMemberName;
    // } else {
    // label = referencedMember.toString();
    // }
    // }
    // if (!StringUtils.hasText(label)) {
    // label = referencedClass.getSimpleName().toString();
    // }
    // link(text, url, label, code);
    // }
    // }
    // }
  }

  public void seeTag(final TypeElement containingClass, final SeeTree seeTag) {
    final XmlWriter writer = this.writer;
    // final String name = seeTag.getSimpleName();
    // if (name.startsWith("@link") || name.equals("@see")) {
    // final boolean code = !name.equalsIgnoreCase("@linkplain");
    // String label = seeTag.label();
    //
    // final StringBuffer stringbuffer = new StringBuffer();
    //
    // final String seeTagText = replaceDocRootDir(seeTag.text());
    // if (seeTagText.startsWith("<") || seeTagText.startsWith("\"")) {
    // stringbuffer.append(seeTagText);
    // writer.write(seeTagText);
    // } else {
    // final TypeElement referencedClass = seeTag.referencedClass();
    // final Element referencedMember = seeTag.referencedMember();
    // String referencedMemberName = seeTag.referencedMemberName();
    // if (referencedClass == null) {
    // final PackageElement packageElement = seeTag.referencedPackage();
    // if (packageElement != null && isIncluded(packageElement)) {
    // final Name packageName = packageElement.getSimpleName();
    // if (!StringUtils.hasText(label)) {
    // label = packageName.toString();
    // }
    // link("#" + packageName, label, code);
    // } else {
    // // TODO link to external package or class
    // // String s9 = getCrossPackageLink(referencedClassName);
    // // String s8;
    // // if (s9 != null)
    // // stringbuffer.append(getHyperLink(s9, "", s1.length() != 0 ? s1
    // // : s3, false));
    // // else if ((s8 = getCrossClassLink(referencedClassName,
    // // referencedMemberName, s1, false, "", !plainLink)) != null) {
    // // stringbuffer.append(s8);
    // // } else {
    // // configuration.getDocletSpecificMsg().warning(seeTag.position(),
    // // "doclet.see.class_or_package_not_found", name, s2);
    // // stringbuffer.append(s1.length() != 0 ? s1 : s3);
    // // }
    // }
    // } else {
    // String url = null;
    // final Name className = referencedClass.getQualifiedName();
    // if (isIncluded(referencedClass)) {
    // url = "#" + className;
    // } else {
    // url = getExternalUrl(className);
    // if (!StringUtils.hasText(url)) {
    // label = className.toString();
    // }
    // }
    // if (referencedMember != null) {
    // if (referencedMember instanceof ExecutableElement) {
    // if (referencedMemberName.indexOf('(') < 0) {
    // final ExecutableElement executableDoc =
    // (ExecutableElement)referencedMember;
    // referencedMemberName = referencedMemberName + executableDoc.signature();
    // }
    // if (StringUtils.hasText(referencedMemberName)) {
    // label = referencedMemberName;
    // } else {
    // label = seeTagText;
    // }
    // }
    // if (isIncluded(referencedClass)) {
    // url += "." + referencedMemberName;
    // } else if (StringUtils.hasText(url)) {
    // url += "#" + referencedMemberName;
    // } else {
    // label = referencedMember.toString();
    // }
    // }
    // if (!StringUtils.hasText(label)) {
    // label = referencedClass.getSimpleName().toString();
    // }
    // link(url, label, code);
    // }
    // }
    // }
  }

  public boolean setDestDir(final String destDir) {
    this.destDir = destDir;

    final File file = new File(destDir);
    if (!file.exists()) {
      this.reporter.print(Kind.NOTE, "Create directory" + destDir);
      file.mkdirs();
    }
    if (!file.isDirectory()) {
      this.reporter.print(Kind.ERROR, "Destination not a directory" + file.getPath());
      return false;
    } else if (!file.canWrite()) {
      this.reporter.print(Kind.ERROR, "Destination directory not writable " + file.getPath());
      return false;
    }
    return true;
  }

  public void setDocTitle(final String docTitle) {
    this.docTitle = docTitle;
  }

  public void tagWithAnchor(final QName tag, final String name, final String title) {
    final XmlWriter writer = this.writer;
    writer.startTag(tag);
    writer.attribute(HtmlAttr.CLASS, "title");
    writer.startTag(HtmlElem.A);
    writer.attribute(HtmlAttr.NAME, name);
    writer.text(title);
    writer.endTag(HtmlElem.A);
    writer.endTagLn(tag);
  }

  public void text(final StringBuilder text, final String string) {
    int index = 0;
    final int lastIndex = string.length();
    String escapeString = null;
    for (int i = index; i < lastIndex; i++) {
      final char ch = string.charAt(i);
      switch (ch) {
        case '&':
          escapeString = "&amp;";
        break;
        case '<':
          escapeString = "&lt;";
        break;
        case '>':
          escapeString = "&gt;";
        break;
        case 9:
        case 10:
        case 13:
        // Accept these control characters
        break;
        default:
          // Reject all other control characters
          if (ch < 32) {
            throw new IllegalStateException(
              "character " + Integer.toString(ch) + " is not allowed in output");
          }
        break;
      }
      if (escapeString != null) {
        if (i > index) {
          text.append(string, index, i - index);
        }
        text.append(escapeString);
        escapeString = null;
        index = i + 1;
      }
    }
    if (lastIndex > index) {
      text.append(string, index, lastIndex - index);
    }
  }

  public void title(final CharSequence name, final CharSequence title) {
    final XmlWriter writer = this.writer;
    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "title");
    anchor(name, title);
    writer.endTagLn(HtmlElem.DIV);
  }

  public void title(final QName element, final String title) {
    final XmlWriter writer = this.writer;
    writer.startTag(element);
    writer.startTag(HtmlElem.SPAN);
    writer.attribute(HtmlAttr.CLASS, "label label-primary");
    writer.text(title);
    writer.endTag(HtmlElem.SPAN);
    writer.endTagLn(element);
  }

  public void typeName(final TypeMirror type) {
    final XmlWriter writer = this.writer;
    String typeName;
    final String qualifiedTypeName = getQualifiedName(type).toString();
    if (isTypeIncluded(type) || getExternalUrl(qualifiedTypeName) != null) {
      typeName = getName(type).toString();
    } else {
      typeName = qualifiedTypeName;
    }
    writer.text(typeName);
    writer.text(getDimension(type));
  }

  public void typeNameLink(final TypeMirror type) {
    final XmlWriter writer = this.writer;
    if (type instanceof WildcardType) {
      final WildcardType wildCard = (WildcardType)type;
      writer.text("?");
      final TypeMirror extendsBound = wildCard.getExtendsBound();
      if (extendsBound != null) {
        writer.text(" extends ");
        typeNameLink(extendsBound);
      }
    } else {
      final Name qualifiedTypeName = getQualifiedName(type);
      final String externalLink = getExternalUrl(qualifiedTypeName);

      final boolean included = isTypeIncluded(type);

      if (externalLink != null) {
        HtmlUtil.serializeA(writer, "", externalLink, getName(type));
      } else if (included) {
        final String url = "#" + qualifiedTypeName;
        HtmlUtil.serializeA(writer, "", url, getName(type));
      } else {
        writer.text(qualifiedTypeName);
      }
      if (type instanceof DeclaredType) {
        final DeclaredType parameterizedType = (DeclaredType)type;
        final List<? extends TypeMirror> typeArguments = parameterizedType.getTypeArguments();
        if (typeArguments.size() > 0) {
          writer.text("<");
          int i = 0;
          for (final TypeMirror typeMirror : typeArguments) {
            if (i > 0) {
              writer.text(", ");
            }
            typeNameLink(typeMirror);
            i++;
          }
          writer.text(">");
        }
      }
    }
    writer.text(getDimension(type));
  }

}
