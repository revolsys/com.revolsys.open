package com.revolsys.ui.html.decorator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
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
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "form-group");
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "col-sm-12");

    serializeElement(out, element);
    serializeErrors(out, element);

    final String instructions = getInstructions();
    HtmlUtil.serializeP(out, "help-block", instructions);
    out.endTag(HtmlElem.DIV);
    out.endTag(HtmlElem.DIV);
  }

  protected void serializeElement(final XmlWriter out, final Element element) {
    element.serializeElement(out);
  }

  protected void serializeErrors(final XmlWriter out, final Element element) {
    if (element instanceof Field) {
      final Field field = (Field)element;
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "help-block with-errors");
      out.closeStartTag();
      for (final String error : field.getValidationErrors()) {
        out.startTag(HtmlElem.DIV);
        out.text(error);
        out.endTag(HtmlElem.DIV);
      }
      out.endTag(HtmlElem.DIV);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }

  public void setRequired(final boolean required) {
    this.required = required;
  }
}
