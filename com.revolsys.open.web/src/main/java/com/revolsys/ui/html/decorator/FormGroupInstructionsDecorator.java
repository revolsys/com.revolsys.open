package com.revolsys.ui.html.decorator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlUtil;

public class FormGroupInstructionsDecorator implements Decorator {
  public static void decorate(final ElementContainer container, final Element element,
    final String instructions) {
    final FormGroupInstructionsDecorator decorator = new FormGroupInstructionsDecorator(
      instructions);
    container.add(element, decorator);
  }

  private String instructions = "";

  private boolean required;

  public FormGroupInstructionsDecorator(final String instructions) {
    this.instructions = instructions;
  }

  public String getInstructions() {
    return this.instructions;
  }

  public boolean isRequired() {
    return this.required;
  }

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "form-group");
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "col-sm-12");

    serializeElement(out, element);
    serializeErrors(out, element);

    final String instructions = getInstructions();
    HtmlUtil.serializeP(out, "help-block", instructions);
    out.endTag(HtmlUtil.DIV);
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

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }

  public void setRequired(final boolean required) {
    this.required = required;
  }
}
