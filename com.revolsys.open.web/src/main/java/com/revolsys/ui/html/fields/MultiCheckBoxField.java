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
import com.revolsys.util.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class MultiCheckBoxField extends Field {
  private boolean selected = false;

  private final Object trueValue = Boolean.TRUE;

  private final Object falseValue = Boolean.FALSE;

  private String selectedValue = "on";

  private String onClick = null;

  private boolean defaultValue;

  public MultiCheckBoxField(final String name, final boolean required) {
    super(name, required);
  }

  public MultiCheckBoxField(final String name, final Object selectedValue,
    final boolean defaultValue) {
    this(name, selectedValue.toString(), defaultValue);
  }

  public MultiCheckBoxField(final String name, final String selectedValue,
    final boolean defaultValue) {
    super(name, false);
    this.selectedValue = selectedValue;
    this.defaultValue = defaultValue;
    this.selected = true;
    if (selected) {
      setValue(trueValue);
    } else {
      setValue(falseValue);
    }
  }

  public String getOnClick() {
    return onClick;
  }

  @Override
  public boolean hasValue() {
    return isSelected();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String[] inputValues = request.getParameterValues(getName());
    if (inputValues != null) {
      selected = false;
      for (int i = 0; i < inputValues.length; i++) {
        final String inputValue = inputValues[i];
        if (inputValue.equals(selectedValue)) {
          selected = true;
        }

      }
    } else if (request.getMethod() == "GET" || !getForm().isMainFormTask()) {
      if (defaultValue) {
        selected = true;
        setValue(trueValue);
      } else {
        selected = false;
        setValue(falseValue);
      }
    } else {
      setValue(falseValue);
      selected = false;
    }
  }

  public boolean isSelected() {
    return selected;
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else {
      valid = true;
    }
    if (valid) {
      if (isSelected()) {
        setValue(trueValue);
      } else {
        setValue(falseValue);
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    HtmlUtil.serializeCheckBox(out, getName(), selectedValue, isSelected(),
      onClick);
  }

  public void setOnClick(final String onSelect) {
    this.onClick = onSelect;
  }
}
