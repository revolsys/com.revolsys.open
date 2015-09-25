package com.revolsys.ui.html.decorator;

import java.util.Iterator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlUtil;

public class FieldNoLabelDecorator implements Decorator {
  public static void add(final ElementContainer container, final Element element,
    final String instructions) {

    final FieldNoLabelDecorator decorator = new FieldNoLabelDecorator(instructions);
    element.setDecorator(decorator);
    container.add(element);
  }

  private String instructions = "";

  public FieldNoLabelDecorator() {
  }

  public FieldNoLabelDecorator(final String instructions) {
    this.instructions = instructions;
  }

  public String getInstructions() {
    return this.instructions;
  }

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "fieldComponent");
    serializeElement(out, element);
    serializeInstructions(out);
    serializeErrors(out, element);
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeElement(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "contents");
    element.serializeElement(out);
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeErrors(final XmlWriter out, final Element element) {
    if (element instanceof Field) {
      final Field field = (Field)element;
      if (field.hasValidationErrors()) {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, "errors");
        for (final Iterator<String> validationErrors = field.getValidationErrors()
          .iterator(); validationErrors.hasNext();) {
          final String error = validationErrors.next();
          out.text(error);
          if (validationErrors.hasNext()) {
            out.text(", ");
          }
        }
        out.endTag(HtmlUtil.DIV);
      }
    }
  }

  protected void serializeInstructions(final XmlWriter out) {
    final String instructions = getInstructions();
    if (instructions != null) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "instructions");
      out.text(instructions);
      out.endTag(HtmlUtil.DIV);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }
}
