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

public class IntegerField extends Field {
  private int size = 10;

  private String inputValue = "";

  private int minimumValue = Integer.MIN_VALUE;

  private int maximumValue = Integer.MAX_VALUE;

  public IntegerField(final String name, final boolean required) {
    super(name, required);
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
      if (getValue() != null) {
        inputValue = getValue().toString();
      }
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
      if (inputValue != null && inputValue.trim().length() > 0) {
        try {
          Integer intValue = new Integer(inputValue);
          if (intValue.intValue() < minimumValue) {
            addValidationError("Must be >= " + minimumValue);
            valid = false;
          } else if (intValue.intValue() > maximumValue) {
            addValidationError("Must be <= " + maximumValue);
            valid = false;
          } else {
            setValue(intValue);
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

  /**
   * @return Returns the maximumValue.
   */
  public int getMaximumValue() {
    return maximumValue;
  }

  /**
   * @param maximumValue The maximumValue to set.
   */
  public void setMaximumValue(final int maximumValue) {
    this.maximumValue = maximumValue;
  }

  /**
   * @return Returns the minimumValue.
   */
  public int getMinimumValue() {
    return minimumValue;
  }

  /**
   * @param minimumValue The minimumValue to set.
   */
  public void setMinimumValue(final int minimumValue) {
    this.minimumValue = minimumValue;
  }

}
