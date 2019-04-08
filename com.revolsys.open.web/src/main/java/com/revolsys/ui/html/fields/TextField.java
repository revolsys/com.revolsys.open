package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class TextField extends Field {
  private String cssClass = "";

  private String inputValue = "";

  private int maxLength = Integer.MAX_VALUE;

  private int minLength = 0;

  private int size = 25;

  private String style = null;

  private String type = "text";

  public TextField() {
  }

  public TextField(final String name, final boolean required) {
    super(name, required);
    this.size = 25;
  }

  public TextField(final String name, final int size, final boolean required) {
    super(name, required);
    this.size = size;
  }

  public TextField(final String name, final int size, final int maxLength, final boolean required) {
    this(name, size, required);
    this.maxLength = maxLength;
  }

  public TextField(final String name, final int size, final int minLength, final int maxLength,
    final boolean required) {
    this(name, size, required);
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public TextField(final String name, final int size, final int minLength, final int maxLength,
    final String defaultValue, final boolean required) {
    this(name, size, maxLength, defaultValue, required);
    if (minLength > maxLength) {
      throw new IllegalArgumentException(
        "minLength (" + minLength + ") must be <= maxLength (" + minLength + ")");
    }
    this.minLength = minLength;
  }

  public TextField(final String name, final int size, final int maxLength,
    final Object defaultValue, final boolean required) {
    this(name, size, defaultValue, required);
    this.maxLength = maxLength;
  }

  public TextField(final String name, final int size, final int maxLength,
    final String defaultValue, final boolean required) {
    this(name, size, defaultValue, required);
    this.maxLength = maxLength;
  }

  public TextField(final String name, final int size, final Object defaultValue,
    final boolean required) {
    super(name, required);
    this.size = size;
    if (defaultValue != null) {
      setValue(defaultValue);
      setInitialValue(defaultValue.toString());
    }
  }

  public TextField(final String name, final int size, final String defaultValue,
    final boolean required) {
    super(name, required);
    this.size = size;
    setValue(defaultValue);
    setInitialValue(defaultValue);
  }

  public String getCssClass() {
    return this.cssClass;
  }

  public String getInputValue() {
    return this.inputValue;
  }

  /**
   * @return Returns the maxLength.
   */
  public final int getMaxLength() {
    return this.maxLength;
  }

  public final int getMinLength() {
    return this.minLength;
  }

  /**
   * @return Returns the size.
   */
  public final int getSize() {
    return this.size;
  }

  public String getStringValue() {
    return (String)getValue();
  }

  public String getStyle() {
    return this.style;
  }

  public String getType() {
    return this.type;
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
      final int length = this.inputValue.length();
      if (length > this.maxLength) {
        addValidationError("Cannot exceed " + this.maxLength + " characters");
        valid = false;
      } else if (length < this.minLength) {
        addValidationError("Must be at least " + this.minLength + " characters");
        valid = false;
      }
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

  protected void serializeAttributes(final XmlWriter out) {
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.TYPE, this.type);
    if (this.size > 0) {
      out.attribute(HtmlAttr.SIZE, Integer.toString(this.size));
    }
    if (this.maxLength > 0 && this.maxLength < Integer.MAX_VALUE) {
      out.attribute(HtmlAttr.MAX_LENGTH, Integer.toString(this.maxLength));
    }
    if (Property.hasValue(this.inputValue)) {
      out.attribute(HtmlAttr.VALUE, this.inputValue);
    }
    if (Property.hasValue(this.style)) {
      out.attribute(HtmlAttr.STYLE, this.style);
    }
    final String cssClass = getCssClass();
    out.attribute(HtmlAttr.CLASS, "form-control input-sm " + cssClass);
    if (isRequired()) {
      out.attribute(HtmlAttr.REQUIRED, true);
    }
    serializeAttributes(out);
    out.endTag(HtmlElem.INPUT);
  }

  public void setCssClass(final String cssClass) {
    if (Property.hasValue(cssClass)) {
      this.cssClass = cssClass;
    } else {
      this.cssClass = "";
    }
  }

  protected void setInputValue(final String inputValue) {
    this.inputValue = inputValue;
  }

  public void setMaxLength(final int maxLength) {
    this.maxLength = maxLength;
  }

  public void setMinLength(final int minLength) {
    this.minLength = minLength;
  }

  public void setSize(final int size) {
    this.size = size;
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

  public void setType(final String type) {
    this.type = type;
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      this.inputValue = DataTypes.toString(value);
    } else {
      this.inputValue = null;
    }
  }

}
