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

public class MultiCheckBoxField extends Field {
  private boolean defaultValue;

  private final Object falseValue = Boolean.FALSE;

  private String onClick = null;

  private boolean selected = false;

  private String selectedValue = "on";

  private final Object trueValue = Boolean.TRUE;

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
    if (this.selected) {
      setValue(this.trueValue);
    } else {
      setValue(this.falseValue);
    }
  }

  public String getOnClick() {
    return this.onClick;
  }

  @Override
  public boolean hasValue() {
    return isSelected();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String[] inputValues = request.getParameterValues(getName());
    if (inputValues != null) {
      this.selected = false;
      for (final String inputValue : inputValues) {
        if (inputValue.equals(this.selectedValue)) {
          this.selected = true;
        }

      }
    } else if (request.getMethod() == "GET" || !getForm().isMainFormTask()) {
      if (this.defaultValue) {
        this.selected = true;
        setValue(this.trueValue);
      } else {
        this.selected = false;
        setValue(this.falseValue);
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

  public void setOnClick(final String onSelect) {
    this.onClick = onSelect;
  }
}
