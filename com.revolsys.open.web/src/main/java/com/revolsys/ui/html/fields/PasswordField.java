package com.revolsys.ui.html.fields;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.Property;

public class PasswordField extends TextField {

  /**
   * @param name
   * @param required
   */
  public PasswordField(final String name, final boolean required) {
    super(name, required);
  }

  public PasswordField(final String name, final int minLength,
    final int maxLength, final boolean required) {
    super(name, maxLength, minLength, maxLength, "", required);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "password");
    if (getMaxLength() > 0 && getMaxLength() < Integer.MAX_VALUE) {
      out.attribute(HtmlUtil.ATTR_MAX_LENGTH, getMaxLength());
    }
    if (getSize() > 0) {
      out.attribute(HtmlUtil.ATTR_SIZE, getSize());
    }
    if (Property.hasValue(getStyle())) {
      out.attribute(HtmlUtil.ATTR_STYLE, getStyle());
    }
    if (isRequired()) {
      out.attribute(HtmlUtil.ATTR_CLASS, "required");
    }

    out.endTag(HtmlUtil.INPUT);
  }
}
