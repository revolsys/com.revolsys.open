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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class TextAreaField extends Field {
  private int cols = 50;

  private String inputValue = "";

  private int maxLength = Integer.MAX_VALUE;

  private int minLength = 0;

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

  public TextAreaField(final String name, final int cols, final int rows, final boolean required) {
    super(name, required);
    this.cols = cols;
    this.rows = rows;
  }

  public TextAreaField(final String name, final int cols, final int rows, final int maxLength,
    final boolean required) {
    this(name, cols, rows, required);
    this.maxLength = maxLength;
  }

  public TextAreaField(final String name, final int cols, final int rows, final int minLength,
    final int maxLength, final String defaultValue, final boolean required) {
    this(name, cols, rows, maxLength, defaultValue, required);
    if (minLength <= maxLength) {
      throw new IllegalArgumentException(
        "minLength (" + minLength + ") must be <= maxLength (" + minLength + ")");
    }
    this.minLength = minLength;
  }

  public TextAreaField(final String name, final int cols, final int rows, final int maxLength,
    final String defaultValue, final boolean required) {
    this(name, cols, rows, defaultValue, required);
    this.maxLength = maxLength;
  }

  public TextAreaField(final String name, final int cols, final int rows, final String defaultValue,
    final boolean required) {
    this(name, cols, rows, required);
    this.inputValue = defaultValue;
  }

  public int getCols() {
    return this.cols;
  }

  public String getInputValue() {
    return this.inputValue;
  }

  public final int getMaxLength() {
    return this.maxLength;
  }

  public int getMinLength() {
    return this.minLength;
  }

  public int getRows() {
    return this.rows;
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
      } else {
        setValue(this.inputValue);
      }
    }
    if (!valid) {
      setValue(null);
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.TEXT_AREA);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    out.attribute(HtmlAttr.COLS, Integer.toString(this.cols));
    out.attribute(HtmlAttr.ROWS, Integer.toString(this.rows));
    if (isRequired()) {
      out.attribute(HtmlAttr.REQUIRED, true);
    }
    if (Property.hasValue(this.inputValue)) {
      out.text(this.inputValue);
    } else {
      out.text("");
    }
    out.endTag(HtmlElem.TEXT_AREA);
  }

  public void setCols(final int cols) {
    this.cols = cols;
  }

  public void setMaxLength(final int maxLength) {
    this.maxLength = maxLength;
  }

  public void setMinLength(final int minLength) {
    this.minLength = minLength;
  }

  public void setRows(final int rows) {
    this.rows = rows;
  }
}
