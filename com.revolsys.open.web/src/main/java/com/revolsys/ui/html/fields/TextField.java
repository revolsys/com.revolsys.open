package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class TextField extends Field {
  private int size = 25;

  private String style = null;

  private int minLength = 0;

  private int maxLength = Integer.MAX_VALUE;

  private String inputValue = "";

  private String cssClass;

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

  public TextField(final String name, final int size, final int maxLength,
    final boolean required) {
    this(name, size, required);
    this.maxLength = maxLength;
  }

  public TextField(final String name, final int size, final int minLength,
    final int maxLength, final boolean required) {
    this(name, size, required);
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public TextField(final String name, final int size, final int minLength,
    final int maxLength, final String defaultValue, final boolean required) {
    this(name, size, maxLength, defaultValue, required);
    if (minLength > maxLength) {
      throw new IllegalArgumentException("minLength (" + minLength
        + ") must be <= maxLength (" + minLength + ")");
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

  public TextField(final String name, final int size,
    final Object defaultValue, final boolean required) {
    super(name, required);
    this.size = size;
    if (defaultValue != null) {
      setValue(defaultValue);
      setInitialValue(defaultValue.toString());
    }
  }

  public TextField(final String name, final int size,
    final String defaultValue, final boolean required) {
    super(name, required);
    this.size = size;
    setValue(defaultValue);
    setInitialValue(defaultValue);
  }

  public String getCssClass() {
    return cssClass;
  }

  public String getInputValue() {
    return inputValue;
  }

  /**
   * @return Returns the maxLength.
   */
  public final int getMaxLength() {
    return maxLength;
  }

  public final int getMinLength() {
    return minLength;
  }

  /**
   * @return Returns the size.
   */
  public final int getSize() {
    return size;
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
      final int length = inputValue.length();
      if (length > maxLength) {
        addValidationError("Cannot exceed " + maxLength + " characters");
        valid = false;
      } else if (length < minLength) {
        addValidationError("Must be at least " + minLength + " characters");
        valid = false;
      }
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
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    if (size > 0) {
      out.attribute(HtmlUtil.ATTR_SIZE, Integer.toString(size));
    }
    if (maxLength > 0 && maxLength < Integer.MAX_VALUE) {
      out.attribute(HtmlUtil.ATTR_MAX_LENGTH, Integer.toString(maxLength));
    }
    if (StringUtils.hasText(inputValue)) {
      out.attribute(HtmlUtil.ATTR_VALUE, inputValue);
    }
    if (StringUtils.hasText(style)) {
      out.attribute(HtmlUtil.ATTR_STYLE, style);
    }
    String cssClass = getCssClass();
    if (isRequired()) {
      if (StringUtils.hasText(cssClass)) {
        cssClass += " required";
      } else {
        cssClass = "required";
      }
    }
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);

    out.endTag(HtmlUtil.INPUT);
  }

  public void setCssClass(final String cssClass) {
    this.cssClass = cssClass;
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
      inputValue = value.toString();
    } else {
      inputValue = null;
    }
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      inputValue = StringConverterRegistry.toString(value);
    } else {
      inputValue = null;
    }
  }

}
