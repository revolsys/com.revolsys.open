package com.revolsys.ui.html.fields;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class PasswordField extends TextField {

  /**
   * @param name
   * @param required
   */
  public PasswordField(final String name, final boolean required) {
    super(name, required);
  }

  public PasswordField(final String name, final int minLength, final int maxLength,
    final boolean required) {
    super(name, maxLength, minLength, maxLength, "", required);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.TYPE, "password");
    if (getMaxLength() > 0 && getMaxLength() < Integer.MAX_VALUE) {
      out.attribute(HtmlAttr.MAX_LENGTH, getMaxLength());
    }
    if (getSize() > 0) {
      out.attribute(HtmlAttr.SIZE, getSize());
    }
    if (Property.hasValue(getStyle())) {
      out.attribute(HtmlAttr.STYLE, getStyle());
    }
    final String cssClass = getCssClass();
    out.attribute(HtmlAttr.CLASS, "form-control input-sm " + cssClass);
    if (isRequired()) {
      out.attribute(HtmlAttr.REQUIRED, true);
    }

    out.endTag(HtmlElem.INPUT);
  }
}
