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

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;
import com.revolsys.xml.io.XmlWriter;

public class BigDecimalField extends Field {
  private int size = 10;

  private String inputValue = "";

  private BigDecimal minimumValue;

  private BigDecimal maximumValue;

  private int scale;

  public BigDecimalField(final String name, final boolean required) {
    super(name, required);
  }

  public BigDecimalField(final String name, final int scale,
    final boolean required) {
    super(name, required);
    this.scale = scale;
  }

  public String getInputValue() {
    return inputValue;
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

  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    out.attribute(HtmlUtil.ATTR_SIZE, Integer.toString(size));
    out.attribute(HtmlUtil.ATTR_MAX_LENGTH, Integer.toString(size));
    if (inputValue != null) {
      out.attribute(HtmlUtil.ATTR_VALUE, inputValue);
    }
    out.endTag(HtmlUtil.INPUT);
  }

  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      int length = inputValue.length();
      if (length > size) {
        addValidationError("Cannot exceed " + size + " characters");
        valid = false;
      }
    }
    if (valid) {
      if (hasValue()) {
        try {
          setValue(new BigDecimal(inputValue));
          BigDecimal numericValue = (BigDecimal)getValue();
          if ((numericValue).scale() > scale) {
            addValidationError("Scale must be <= " + scale);
            valid = false;
          } else if (minimumValue != null
            && numericValue.compareTo(minimumValue) < 0) {
            addValidationError("Must be >= " + minimumValue);
            valid = false;
          } else if (maximumValue != null
            && numericValue.compareTo(maximumValue) > 0) {
            addValidationError("Must be <= " + maximumValue);
            valid = false;
          }
        } catch (NumberFormatException e) {
          addValidationError("Must be a valid number");
          valid = false;
        }
      } else {
        setValue(null);
      }
    }
    return valid;
  }

  public BigDecimal getMaximumValue() {
    return maximumValue;
  }

  public void setMaximumValue(final BigDecimal maximumValue) {
    this.maximumValue = maximumValue;
  }

  public BigDecimal getMinimumValue() {
    return minimumValue;
  }

  public void setMinimumValue(final BigDecimal minimumValue) {
    this.minimumValue = minimumValue;
  }

  /**
   * @return Returns the scale.
   */
  public final int getScale() {
    return scale;
  }

  /**
   * @return Returns the size.
   */
  public final int getSize() {
    return size;
  }

  public void setScale(final int scale) {
    this.scale = scale;
  }

  public void setSize(final int size) {
    this.size = size;
  }

  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      inputValue = value.toString();
    }
  }

  /**
   * @param inputValue The inputValue to set.
   */
  protected final void setInputValue(final String inputValue) {
    this.inputValue = inputValue;
  }

}
