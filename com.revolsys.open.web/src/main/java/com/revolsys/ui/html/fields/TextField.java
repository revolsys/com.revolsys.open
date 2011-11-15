/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.fields;


import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class TextField extends Field {
  private int size = 25;

  private String style = null;

  private int minLength = 0;

  private int maxLength = Integer.MAX_VALUE;

  private String inputValue = "";

  private String defaultValue;

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

  public TextField(final String name, final int size,
    final String defaultValue, final boolean required) {
    super(name, required);
    this.size = size;
    setInitialValue(defaultValue);
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

  public TextField(final String name, final int size, final int maxLength,
    final String defaultValue, final boolean required) {
    this(name, size, defaultValue, required);
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

  public boolean hasValue() {
    return inputValue != null && !inputValue.equals("");
  }

  public void initialize(final Form form, final HttpServletRequest request) {
    inputValue = request.getParameter(getName());
    if (inputValue == null) {
      setValue(getInitialValue(request));
    }
  }

  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      inputValue = value.toString();
    } else {
      inputValue = null;
    }
  }

  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    out.attribute(HtmlUtil.ATTR_SIZE, Integer.toString(size));
    if (maxLength < Integer.MAX_VALUE) {
      out.attribute(HtmlUtil.ATTR_MAX_LENGTH, Integer.toString(maxLength));
    }
    if (inputValue != null) {
      out.attribute(HtmlUtil.ATTR_VALUE, inputValue);
    }
    if (style != null) {
      out.attribute(HtmlUtil.ATTR_STYLE, style);
    }
    out.endTag(HtmlUtil.INPUT);
  }

  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      int length = inputValue.length();
      if (length > maxLength) {
        addValidationError("Cannot exceed " + maxLength + " characters");
        valid = false;
      } else if (length < minLength) {
        addValidationError("Must be at least " + minLength + " characters");
        valid = false;
      }
    }
    if (valid) {
      if (inputValue != null && inputValue.length() > 0) {
        super.setValue(inputValue);
      } else {
        super.setValue(null);
      }
    }
    return valid;
  }

  protected void setInputValue(final String inputValue) {
    this.inputValue = inputValue;
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

  public void setStyle(String style) {
    this.style = style;
  }

}
