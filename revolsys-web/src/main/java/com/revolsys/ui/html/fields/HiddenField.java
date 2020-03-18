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
import com.revolsys.util.HtmlUtil;

public class HiddenField extends Field {
  private boolean fixedValue = false;

  private String inputValue = "";

  /**
   * @param name
   * @param required
   */
  public HiddenField(final String name, final boolean required) {
    super(name, required);
  }

  public HiddenField(final String name, final Object value) {
    this(name, value.toString());
  }

  public HiddenField(final String name, final String value) {
    super(name, false);
    this.inputValue = value;
    this.fixedValue = true;
  }

  public String getInputValue() {
    return this.inputValue;
  }

  @Override
  public boolean hasValue() {
    return this.inputValue != null && !this.inputValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    if (!this.fixedValue) {
      this.inputValue = request.getParameter(getName());
      if (this.inputValue == null) {
        setValue(getInitialValue(request));
        if (getValue() != null) {
          this.inputValue = getValue().toString();
        }
      }
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    }
    if (valid) {
      setValue(this.inputValue);
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    HtmlUtil.serializeHiddenInput(out, getName(), this.inputValue);
  }
}
