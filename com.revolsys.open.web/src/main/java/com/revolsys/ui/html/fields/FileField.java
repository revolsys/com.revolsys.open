package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class FileField extends Field {

  private String style = null;

  private String inputValue = "";

  public FileField() {
  }

  public FileField(final String name, final boolean required) {
    super(name, required);
  }

  public String getInputValue() {
    return this.inputValue;
  }

  public String getStringValue() {
    return (String)getValue();
  }

  public String getStyle() {
    return this.style;
  }

  @Override
  public boolean hasValue() {
    return this.inputValue != null && !this.inputValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.inputValue = request.getParameter(getName());
    if (this.inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        this.inputValue = getValue().toString();
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
        if (this.inputValue != null && this.inputValue.length() > 0) {
          setTextValue(this.inputValue);
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
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "file");
    if (Property.hasValue(this.style)) {
      out.attribute(HtmlUtil.ATTR_STYLE, this.style);
    }
    if (isRequired()) {
      out.attribute(HtmlUtil.ATTR_CLASS, "required");
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
      this.inputValue = value.toString();
    } else {
      this.inputValue = null;
    }
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      this.inputValue = value.toString();
    } else {
      this.inputValue = null;
    }
  }

}
