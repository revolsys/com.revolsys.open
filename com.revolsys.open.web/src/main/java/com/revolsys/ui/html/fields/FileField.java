package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class FileField extends Field {

  private String inputValue = "";

  private String style = null;

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
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.TYPE, "file");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    if (Property.hasValue(this.style)) {
      out.attribute(HtmlAttr.STYLE, this.style);
    }
    if (isRequired()) {
      out.attribute(HtmlAttr.REQUIRED, true);
    }
    out.endTag(HtmlElem.INPUT);
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
