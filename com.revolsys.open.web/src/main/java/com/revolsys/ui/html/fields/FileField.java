package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class FileField extends Field {

  private String style = null;

  private String inputValue = "";

  public FileField() {
  }

  public FileField(final String name, final boolean required) {
    super(name, required);
  }

  public String getInputValue() {
    return inputValue;
  }

  public String getStringValue() {
    return (String)getValue();
  }

  public String getStyle() {
    return style;
  }

  @Override
  public boolean hasValue() {
    return inputValue != null && !inputValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    inputValue = request.getParameter(getName());
    if (inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        inputValue = getValue().toString();
      }
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {

    }
    if (valid) {
      try {
        if (inputValue != null && inputValue.length() > 0) {
          setTextValue(inputValue);
        } else {
          setTextValue(null);
        }
      } catch (final IllegalArgumentException e) {
        addValidationError(e.getMessage());
        valid = false;
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "file");
    if (style != null) {
      out.attribute(HtmlUtil.ATTR_STYLE, style);
    }
    out.endTag(HtmlUtil.INPUT);
  }

  protected void setInputValue(final String inputValue) {
    this.inputValue = inputValue;
  }

  public void setStyle(final String style) {
    this.style = style;
  }

  public void setTextValue(final String value) {
    super.setValue(value);
    if (value != null) {
      inputValue = value.toString();
    } else {
      inputValue = null;
    }
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      inputValue = value.toString();
    } else {
      inputValue = null;
    }
  }

}
