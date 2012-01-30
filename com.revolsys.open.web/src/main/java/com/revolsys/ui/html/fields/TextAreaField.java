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

public class TextAreaField extends Field {
  private int cols = 50;

  private int minLength = 0;

  private int maxLength = Integer.MAX_VALUE;

  private String inputValue = "";

  private int rows = 5;

  public TextAreaField() {
  }

  /**
   * @param name
   * @param required
   */
  public TextAreaField(final String name, final boolean required) {
    super(name, required);
  }

  public TextAreaField(final String name, final int cols, final int rows,
    final boolean required) {
    super(name, required);
    this.cols = cols;
    this.rows = rows;
  }

  public TextAreaField(final String name, final int cols, final int rows,
    final String defaultValue, final boolean required) {
    this(name, cols, rows, required);
    this.inputValue = defaultValue;
  }

  public TextAreaField(final String name, final int cols, final int rows,
    final int maxLength, final boolean required) {
    this(name, cols, rows, required);
    this.maxLength = maxLength;
  }

  public TextAreaField(final String name, final int cols, final int rows,
    final int maxLength, final String defaultValue, final boolean required) {
    this(name, cols, rows, defaultValue, required);
    this.maxLength = maxLength;
  }

  public TextAreaField(final String name, final int cols, final int rows,
    final int minLength, final int maxLength, final String defaultValue,
    final boolean required) {
    this(name, cols, rows, maxLength, defaultValue, required);
    if (minLength <= maxLength) {
      throw new IllegalArgumentException("minLength (" + minLength
        + ") must be <= maxLength (" + minLength + ")");
    }
    this.minLength = minLength;
  }

  public int getCols() {
    return cols;
  }

  public String getInputValue() {
    return inputValue;
  }

  public final int getMaxLength() {
    return maxLength;
  }

  public int getMinLength() {
    return minLength;
  }

  public int getRows() {
    return rows;
  }

  public boolean hasValue() {
    return inputValue != null && !inputValue.equals("");
  }

  public void initialize(final Form form, final HttpServletRequest request) {
    inputValue = request.getParameter(getName());
    if (inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        inputValue = getValue().toString();
      }
    }
  }

  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.TEXT_AREA);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_COLS, Integer.toString(cols));
    out.attribute(HtmlUtil.ATTR_ROWS, Integer.toString(rows));
    if (inputValue != null) {
      out.text(inputValue);
    } else {
      out.text("");
    }
    out.endTag(HtmlUtil.TEXT_AREA);
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
      } else {
        setValue(inputValue);
      }
    }
    if (!valid) {
      setValue(null);
    }
    return valid;
  }
}
