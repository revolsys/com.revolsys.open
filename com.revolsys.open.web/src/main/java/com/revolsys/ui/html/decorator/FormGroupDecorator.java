package com.revolsys.ui.html.decorator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class FormGroupDecorator implements Decorator {
  public static void decorate(final ElementContainer container, final Element element,
    final String label, final String instructions) {
    decorate(container, element, null, label, instructions);
  }

  public static void decorate(final ElementContainer container, final Element element,
    final String labelUrl, final String label, final String instructions) {
    final FormGroupDecorator decorator = new FormGroupDecorator(labelUrl, label, instructions);
    container.add(element, decorator);
  }

  private String instructions = "";

  private String label = "";

  private String labelUrl;

  private boolean required;

  public FormGroupDecorator(final String label) {
    this.label = label;
  }

  public FormGroupDecorator(final String label, final String instructions) {
    this.label = label;
    this.instructions = instructions;
  }

  public FormGroupDecorator(final String labelUrl, final String label, final String instructions) {
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
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "form-group");

    serializeLabel(out, element);

    {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "col-sm-9");

      serializeElement(out, element);
      serializeErrors(out, element);

      final String instructions = getInstructions();
      HtmlUtil.serializeP(out, "help-block", instructions);
      out.endTag(HtmlUtil.DIV);
    }
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeElement(final XmlWriter out, final Element element) {
    element.serializeElement(out);
  }

  protected void serializeErrors(final XmlWriter out, final Element element) {
    if (element instanceof Field) {
      final Field field = (Field)element;
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "help-block with-errors");
      out.closeStartTag();
      for (final String error : field.getValidationErrors()) {
        out.startTag(HtmlUtil.DIV);
        out.text(error);
        out.endTag(HtmlUtil.DIV);
      }
      out.endTag(HtmlUtil.DIV);
    }
  }

  protected void serializeLabel(final XmlWriter out, final Element element) {
    final String label = getLabel();
    if (label != null) {
      out.startTag(HtmlUtil.LABEL);

      if (element instanceof Field) {
        final Field field = (Field)element;
        out.attribute(HtmlUtil.ATTR_FOR, field.getName());
      }
      out.attribute(HtmlUtil.ATTR_CLASS, "col-sm-3 control-label");
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
