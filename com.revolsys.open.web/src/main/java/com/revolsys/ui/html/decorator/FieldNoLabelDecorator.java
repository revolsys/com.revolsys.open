package com.revolsys.ui.html.decorator;

import java.util.Iterator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

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
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "fieldComponent");
    serializeElement(out, element);
    serializeInstructions(out);
    serializeErrors(out, element);
    out.endTag(HtmlElem.DIV);
  }

  protected void serializeElement(final XmlWriter out, final Element element) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "contents");
    element.serializeElement(out);
    out.endTag(HtmlElem.DIV);
  }

  protected void serializeErrors(final XmlWriter out, final Element element) {
    if (element instanceof Field) {
      final Field field = (Field)element;
      if (field.hasValidationErrors()) {
        out.startTag(HtmlElem.DIV);
        out.attribute(HtmlAttr.CLASS, "errors");
        for (final Iterator<String> validationErrors = field.getValidationErrors()
          .iterator(); validationErrors.hasNext();) {
          final String error = validationErrors.next();
          out.text(error);
          if (validationErrors.hasNext()) {
            out.text(", ");
          }
        }
        out.endTag(HtmlElem.DIV);
      }
    }
  }

  protected void serializeInstructions(final XmlWriter out) {
    final String instructions = getInstructions();
    if (instructions != null) {
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "instructions");
      out.text(instructions);
      out.endTag(HtmlElem.DIV);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }
}
