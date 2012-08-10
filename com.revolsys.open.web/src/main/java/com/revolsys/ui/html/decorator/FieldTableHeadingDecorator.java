package com.revolsys.ui.html.decorator;

import java.util.Iterator;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.fields.EmailAddressField;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.TableRow;

public class FieldTableHeadingDecorator implements Decorator {
  private String label = "";

  private String instructions = "";

  public FieldTableHeadingDecorator(final String label) {
    this.label = label;
  }

  public FieldTableHeadingDecorator(final String label,
    final String instructions) {
    this.label = label;
    this.instructions = instructions;
  }

  public String getInstructions() {
    return instructions;
  }

  public String getLabel() {
    return label;
  }

  public void serialize(final XmlWriter out, final Element element) {
    final Field field = (Field)element;
    out.startTag(HtmlUtil.TH);
    serializeLabel(out, field);
    out.endTag(HtmlUtil.TH);
    out.startTag(HtmlUtil.TD);
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "fieldComponent");
    serializeField(out, field);
    serializeInstructions(out);
    serializeErrors(out, field);
    out.endTag(HtmlUtil.DIV);
    out.endTag(HtmlUtil.TD);
  }

  protected void serializeErrors(final XmlWriter out, final Field field) {
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

  protected void serializeField(final XmlWriter out, final Field field) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "contents");
    field.serializeElement(out);
    out.endTag(HtmlUtil.DIV);
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

  protected void serializeLabel(final XmlWriter out, final Field field) {
    final String label = getLabel();
    if (label != null) {
      out.startTag(HtmlUtil.DIV);
      if (field.isRequired()) {
        out.attribute(HtmlUtil.ATTR_CLASS, "label required");
      } else {
        out.attribute(HtmlUtil.ATTR_CLASS, "label");
      }
      out.startTag(HtmlUtil.LABEL);
      out.attribute(HtmlUtil.ATTR_FOR, field.getName());
      out.text(label);
      out.endTag(HtmlUtil.LABEL);
      out.endTag(HtmlUtil.DIV);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public static void addRow(Form form, Field field, String label,
    String instructions) {

    FieldTableHeadingDecorator decorator = new FieldTableHeadingDecorator(
      label, instructions);
    TableRow row = new TableRow();
    row.add(field, decorator);
    form.add(row);
  }
}
