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

public class CheckBoxField extends Field {
  private Object falseValue = Boolean.FALSE;

  private String onClick = null;

  private boolean selected = false;

  private final String selectedValue = "on";

  private Object trueValue = Boolean.TRUE;

  public CheckBoxField() {
  }

  public CheckBoxField(final String name) {
    super(name, false);
  }

  public CheckBoxField(final String name, final boolean required) {
    super(name, required);
  }

  public CheckBoxField(final String name, final boolean required, final Object defaultValue) {
    super(name, required);
    if (defaultValue != null) {
      if (defaultValue != null) {
        this.selected = Boolean.valueOf(defaultValue.toString());
      }
    }
  }

  public Object getFalseValue() {
    return this.falseValue;
  }

  public String getOnClick() {
    return this.onClick;
  }

  public Object getTrueValue() {
    return this.trueValue;
  }

  @Override
  public boolean hasValue() {
    return isSelected();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String inputValue = request.getParameter(getName());
    if (inputValue != null) {
      this.selected = inputValue.equals(this.selectedValue);
    } else if (request.getMethod() == "GET" || !getForm().isMainFormTask()) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        this.selected = getValue().equals(this.trueValue);
      }
    } else {
      setValue(this.falseValue);
      this.selected = false;
    }
  }

  public boolean isSelected() {
    return this.selected;
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
        setValue(this.trueValue);
      } else {
        setValue(this.falseValue);
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    HtmlUtil.serializeCheckBox(out, getName(), this.selectedValue, isSelected(), this.onClick);
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
