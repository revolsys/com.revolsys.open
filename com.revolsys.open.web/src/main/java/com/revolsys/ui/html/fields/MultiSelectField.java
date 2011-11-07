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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;
import com.revolsys.xml.io.XmlWriter;

public class MultiSelectField extends Field {
  private static final Logger log = Logger.getLogger(MultiSelectField.class);

  private boolean hasInvalidOptions = false;

  private Map selectedValues = new HashMap();

  private List options = new ArrayList();

  private Map optionMap = new HashMap();

  private Map optionValueMap = new HashMap();

  private String onChange;

  private int size = 4;

  /**
   * @param name
   * @param required
   */
  public MultiSelectField(final String name, final boolean required) {
    super(name, required);
  }

  public void addOption(final String label) {
    addOption(label, label);
  }

  public void addOption(final Object value, final String label) {
    String stringValue = null;
    if (value != null) {
      stringValue = value.toString();
    }
    addOption(value, stringValue, label);
  }

  public void addOption(final String value, final String label) {
    addOption(value, value, label);
  }

  public void addOption(final Object value, final String stringValue,
    final String label) {
    FieldValue option = new FieldValue(value, stringValue, label);
    options.add(option);
    optionMap.put(stringValue, option);
    optionValueMap.put(value, option);
  }

  public void addOption(final Object value, final Object stringValue,
    final String label) {
    addOption(value, stringValue.toString(), label);
  }

  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.SELECT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_MULTIPLE, "multiple");
    out.attribute(HtmlUtil.ATTR_SIZE, String.valueOf(size));
    if (onChange != null) {
      out.attribute(HtmlUtil.ATTR_ON_CHANGE, onChange);
    }
    serializeOptions(out);
    out.endTag(HtmlUtil.SELECT);

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "fieldActions");
    String baseUrl = "javascript:setMutliSelectAllSelected('"
      + getForm().getName() + "','" + getName() + "'";
    HtmlUtil.serializeA(out, null, baseUrl + ",true)", "select all");
    HtmlUtil.serializeA(out, null, baseUrl + ",false)", "select none");
    out.endTag(HtmlUtil.DIV);
  }

  private void serializeOptions(final XmlWriter out) {
    for (Iterator optionIter = options.iterator(); optionIter.hasNext();) {
      FieldValue option = (FieldValue)optionIter.next();
      out.startTag(HtmlUtil.OPTION);
      if (selectedValues.containsKey(option.getStringValue())) {
        out.attribute(HtmlUtil.ATTR_SELECTED, "true");
      }
      if (!option.getStringValue().equals(option.getLabel())) {
        out.attribute(HtmlUtil.ATTR_VALUE, option.getStringValue());
      }
      out.text(option.getLabel());
      out.endTag(HtmlUtil.OPTION);
    }
  }

  public void initialize(final Form form, final HttpServletRequest request) {
    String[] parameterValues = request.getParameterValues(getName());
    if (parameterValues == null) {
      setValue(Collections.EMPTY_LIST);
      if (!form.hasTask()) {
        Object initialValue = getInitialValue(request);
        if (initialValue != null) {
          setValue(initialValue);
        }
      }
    } else {
      for (int i = 0; i < parameterValues.length; i++) {
        String stringValue = parameterValues[i];
        FieldValue option = (FieldValue)optionMap.get(stringValue);
        if (option != null) {
          selectedValues.put(option.getStringValue(), option.getValue());
        } else {
          hasInvalidOptions = true;
        }
      }
    }
  }

  public void setValue(final Object object) {
    selectedValues.clear();
    List valueList = (List)object;
    super.setValue(valueList);
    if (valueList != null) {
      for (Iterator values = valueList.iterator(); values.hasNext();) {
        Object value = (Object)values.next();
        FieldValue option = (FieldValue)optionValueMap.get(value);
        if (option != null) {
          selectedValues.put(option.getStringValue(), option.getValue());
        }
      }
    }
  }

  public boolean hasValue() {
    return !selectedValues.isEmpty();
  }

  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      if (hasInvalidOptions) {
        addValidationError("Invalid Value");
        valid = false;
      }
      if (valid) {
        if (selectedValues.isEmpty()) {
          setValue(Collections.EMPTY_LIST);
        } else {
          setValue(new ArrayList(selectedValues.values()));
        }
      }
    }
    return valid;
  }

  public void setOnChange(final String onChange) {
    this.onChange = onChange;
  }
}
