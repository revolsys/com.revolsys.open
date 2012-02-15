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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class SelectField extends Field {
  private static final Logger log = Logger.getLogger(SelectField.class);

  private String defaultValue;

  private String nullValueLabel = "(None)";

  private String onChange;

  private final Map<String, FieldValue> optionMap = new HashMap<String, FieldValue>();

  private final List<FieldValue> options = new ArrayList<FieldValue>();

  private final Map<Object, FieldValue> optionValueMap = new HashMap<Object, FieldValue>();

  private String stringValue;

  public SelectField() {
  }

  public SelectField(final String name, final boolean required) {
    this(name, required, ("(None)"));
  }

  public SelectField(final String name, final boolean required,
    final String nullValueLabel) {
    super(name, required);
    this.nullValueLabel = nullValueLabel;
  }

  public SelectField(final String name, final String defaultValue,
    final boolean required) {
    this(name, required);
    this.defaultValue = defaultValue;
    this.stringValue = defaultValue;
  }

  public void addOption(
    final int index,
    final Object value,
    final String stringValue,
    final String label) {
    final FieldValue option = new FieldValue(value, stringValue, label);
    options.add(index, option);
    optionMap.put(stringValue, option);
    optionValueMap.put(value, option);
  }

  public void addOption(
    final Object value,
    final Object stringValue,
    final String label) {
    addOption(value, stringValue.toString(), label);
  }

  public void addOption(final Object value, final String label) {
    String stringValue = null;
    if (value != null) {
      stringValue = value.toString();
    }
    addOption(value, stringValue, label);
  }

  public void addOption(
    final Object value,
    final String stringValue,
    final String label) {
    final FieldValue option = new FieldValue(value, stringValue, label);
    options.add(option);
    optionMap.put(stringValue, option);
    optionValueMap.put(value, option);
  }

  public void addOption(final String label) {
    addOption(label, label);
  }

  public void addOption(final String value, final String label) {
    addOption(value, value, label);
  }

  public void addOptions(final Map<Object, String> options) {
    for (final Entry<Object, String> entry : options.entrySet()) {
      final Object key = entry.getKey();
      final String label = entry.getValue();
      addOption(key, label);
    }
  }

  @Override
  public SelectField clone() {
    final SelectField field = new SelectField();
    field.setName(getName());
    field.setDefaultValue(getDefaultValue());
    field.setRequired(isRequired());
    field.setReadOnly(isReadOnly());
    field.setNullValueLabel(getNullValueLabel());
    return field;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getNullValueLabel() {
    return nullValueLabel;
  }

  public FieldValue getSelectedOption() {
    return optionMap.get(stringValue);
  }

  public String getStringValue() {
    return stringValue;
  }

  @Override
  public boolean hasValue() {
    return stringValue != null && !stringValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    if (!isRequired()) {
      addOption(0, null, null, nullValueLabel);
    }

    stringValue = request.getParameter(getName());
    if (stringValue == null) {
      setValue(getInitialValue(request));
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      final FieldValue option = getSelectedOption();
      if (option == null) {
        addValidationError("Invalid Value");
        valid = false;
      }
      if (valid) {
        setValue(option.getValue());
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.SELECT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    if (onChange != null) {
      out.attribute(HtmlUtil.ATTR_ON_CHANGE, onChange);
    }
    serializeOptions(out);
    out.endTag(HtmlUtil.SELECT);
  }

  private void serializeOptions(final XmlWriter out) {
    if (options.size() == 0) {
      addOption(null, "(None)");
    }
    for (final FieldValue option : options) {
      out.startTag(HtmlUtil.OPTION);
      if (option.getStringValue().equals(stringValue)) {
        out.attribute(HtmlUtil.ATTR_SELECTED, "true");
      }
      if (!option.getStringValue().equals(option.getLabel())) {
        out.attribute(HtmlUtil.ATTR_VALUE, option.getStringValue());
      }
      out.text(option.getLabel());
      out.endTag(HtmlUtil.OPTION);
    }
  }

  public void setDefaultValue(final String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setNullValueLabel(final String nullValueLabel) {
    this.nullValueLabel = nullValueLabel;
  }

  public void setOnChange(final String onChange) {
    this.onChange = onChange;
  }

  public void setOptions(final Map<Object, String> options) {
    options.clear();
    optionMap.clear();
    optionValueMap.clear();
    for (final Entry<Object, String> entry : options.entrySet()) {
      final Object key = entry.getKey();
      final String label = entry.getValue();
      addOption(key, label);
    }
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      final FieldValue option = optionValueMap.get(value);
      if (option != null) {
        stringValue = option.getStringValue();
      } else {
        super.setValue(null);
        stringValue = null;
      }
    }
  }

}
