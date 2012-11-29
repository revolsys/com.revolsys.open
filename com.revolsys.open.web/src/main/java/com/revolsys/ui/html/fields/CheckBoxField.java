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

public class CheckBoxField extends Field {
  private boolean selected = false;

  private Object trueValue = Boolean.TRUE;

  private Object falseValue = Boolean.FALSE;

  private final String selectedValue = "on";

  private String onClick = null;

  public CheckBoxField() {
  }

  public CheckBoxField(final String name) {
    super(name, false);
  }

  public CheckBoxField(final String name, final boolean required) {
    super(name, required);
  }

  public CheckBoxField(String name, boolean required, Object defaultValue) {
    super(name, required);
    if (defaultValue != null) {
      if (defaultValue != null) {
        selected = Boolean.valueOf(defaultValue.toString());
      }
    }
  }

  public Object getFalseValue() {
    return falseValue;
  }

  public String getOnClick() {
    return onClick;
  }

  public Object getTrueValue() {
    return trueValue;
  }

  @Override
  public boolean hasValue() {
    return isSelected();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String inputValue = request.getParameter(getName());
    if (inputValue != null) {
      selected = inputValue.equals(selectedValue);
    } else if (request.getMethod() == "GET" || !getForm().isMainFormTask()) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        selected = getValue().equals(trueValue);
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

  public void setFalseValue(final Object falseValue) {
    this.falseValue = falseValue;
  }

  public void setOnClick(final String onSelect) {
    this.onClick = onSelect;
  }

  public void setTrueValue(final Object trueValue) {
    this.trueValue = trueValue;
  }
}
