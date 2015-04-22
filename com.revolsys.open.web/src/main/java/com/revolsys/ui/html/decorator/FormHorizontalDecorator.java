package com.revolsys.ui.html.decorator;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class FormHorizontalDecorator implements Decorator {
  public static void add(final ElementContainer container, final Element element,
    final String label, final String instructions) {

    final FormHorizontalDecorator decorator = new FormHorizontalDecorator(label, instructions);
    container.add(element, decorator);
  }

  public static void add(final ElementContainer container, final Element element,
    final String labelUrl, final String label, final String instructions) {

    final FormHorizontalDecorator decorator = new FormHorizontalDecorator(labelUrl, label,
      instructions);
    container.add(element, decorator);
  }

  private String label = "";

  private String instructions = "";

  private boolean required;

  private String labelUrl;

  public FormHorizontalDecorator(final String label) {
    this.label = label;
  }

  public FormHorizontalDecorator(final String label, final String instructions) {
    this.label = label;
    this.instructions = instructions;
  }

  public FormHorizontalDecorator(final String labelUrl, final String label,
    final String instructions) {
    this.labelUrl = labelUrl;
    this.label = label;
    this.instructions = instructions;
  }

  public String getInstructions() {
    return this.instructions;
  }

  public String getLabel() {
    return this.label;
  }

  public String getLabelUrl() {
    return this.labelUrl;
  }

  public boolean isRequired() {
    return this.required;
  }

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "form-group");
      serializeLabel(out, element);
      {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, "col-sm-10");
        element.serializeElement(out);
        serializeErrors(out, element);
        serializeInstructions(out);
        out.endTag(HtmlUtil.DIV);
      }
      out.endTag(HtmlUtil.DIV);
    }
  }

  protected void serializeErrors(final XmlWriter out, final Element element) {
    if (element instanceof Field) {
      final Field field = (Field)element;
      for (final String error : field.getValidationErrors()) {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, "errorMessage");
        out.startTag(HtmlUtil.LABEL);
        out.attribute(HtmlUtil.ATTR_FOR, field.getName());
        out.attribute(HtmlUtil.ATTR_CLASS, "error");
        out.attribute("generated", "true");

        out.text(error);
        out.endTag(HtmlUtil.LABEL);
        out.endTag(HtmlUtil.DIV);
      }
    }
  }

  protected void serializeInstructions(final XmlWriter out) {
    final String instructions = getInstructions();
    if (instructions != null) {
      out.startTag(HtmlUtil.P);
      out.attribute(HtmlUtil.ATTR_CLASS, "help-block");
      out.text(instructions);
      out.endTag(HtmlUtil.P);
    }
  }

  protected void serializeLabel(final XmlWriter out, final Element element) {
    final String label = getLabel();
    if (Property.hasValue(label)) {
      out.startTag(HtmlUtil.LABEL);
      out.attribute(HtmlUtil.ATTR_CLASS, "col-sm-2 control-label");
      if (element instanceof Field) {
        final Field field = (Field)element;
        out.attribute(HtmlUtil.ATTR_FOR, field.getName());
      }
      if (Property.hasValue(this.labelUrl)) {
        out.startTag(HtmlUtil.A);
        out.attribute(HtmlUtil.ATTR_HREF, this.labelUrl);
      }
      out.text(label);
      if (Property.hasValue(this.labelUrl)) {
        out.endTag(HtmlUtil.A);
      }
      out.endTag(HtmlUtil.LABEL);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setLabelUrl(final String labelUrl) {
    this.labelUrl = labelUrl;
  }

  public void setRequired(final boolean required) {
    this.required = required;
  }
}
