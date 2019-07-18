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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;

public class MultiSelectField extends Field {
  private static final Logger log = LoggerFactory.getLogger(MultiSelectField.class);

  private boolean hasInvalidOptions = false;

  private String onChange;

  private final Map optionMap = new HashMap();

  private final List options = new ArrayList();

  private final Map optionValueMap = new HashMap();

  private final Map selectedValues = new HashMap();

  private final int size = 4;

  /**
   * @param name
   * @param required
   */
  public MultiSelectField(final String name, final boolean required) {
    super(name, required);
  }

  public void addOption(final Object value, final Object stringValue, final String label) {
    addOption(value, stringValue.toString(), label);
  }

  public void addOption(final Object value, final String label) {
    String stringValue = null;
    if (value != null) {
      stringValue = value.toString();
    }
    addOption(value, stringValue, label);
  }

  public void addOption(final Object value, final String stringValue, final String label) {
    final FieldValue option = new FieldValue(value, stringValue, label);
    this.options.add(option);
    this.optionMap.put(stringValue, option);
    this.optionValueMap.put(value, option);
  }

  public void addOption(final String label) {
    addOption(label, label);
  }

  public void addOption(final String value, final String label) {
    addOption(value, value, label);
  }

  @Override
  public boolean hasValue() {
    return !this.selectedValues.isEmpty();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String[] parameterValues = request.getParameterValues(getName());
    if (parameterValues == null) {
      setValue(Collections.EMPTY_LIST);
      if (!form.hasTask()) {
        final Object initialValue = getInitialValue(request);
        if (initialValue != null) {
          setValue(initialValue);
        }
      }
    } else {
      for (final String stringValue : parameterValues) {
        final FieldValue option = (FieldValue)this.optionMap.get(stringValue);
        if (option != null) {
          this.selectedValues.put(option.getStringValue(), option.getValue());
        } else {
          this.hasInvalidOptions = true;
        }
      }
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      if (this.hasInvalidOptions) {
        addValidationError("Invalid Value");
        valid = false;
      }
      if (valid) {
        if (this.selectedValues.isEmpty()) {
          setValue(Collections.EMPTY_LIST);
        } else {
          setValue(new ArrayList(this.selectedValues.values()));
        }
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.MULTIPLE, "multiple");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    out.attribute(HtmlAttr.SIZE, String.valueOf(this.size));
    if (this.onChange != null) {
      out.attribute(HtmlAttr.ON_CHANGE, this.onChange);
    }
    serializeOptions(out);
    out.endTag(HtmlElem.SELECT);

    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "fieldActions");
    final String baseUrl = "javascript:setMutliSelectAllSelected('" + getForm().getName() + "','"
      + getName() + "'";
    HtmlUtil.serializeA(out, null, baseUrl + ",true)", "select all");
    HtmlUtil.serializeA(out, null, baseUrl + ",false)", "select none");
    out.endTag(HtmlElem.DIV);
  }

  private void serializeOptions(final XmlWriter out) {
    for (final Iterator optionIter = this.options.iterator(); optionIter.hasNext();) {
      final FieldValue option = (FieldValue)optionIter.next();
      out.startTag(HtmlElem.OPTION);
      if (this.selectedValues.containsKey(option.getStringValue())) {
        out.attribute(HtmlAttr.SELECTED, "true");
      }
      if (!option.getStringValue().equals(option.getLabel())) {
        out.attribute(HtmlAttr.VALUE, option.getStringValue());
      }
      out.text(option.getLabel());
      out.endTag(HtmlElem.OPTION);
    }
  }

  public void setOnChange(final String onChange) {
    this.onChange = onChange;
  }

  @Override
  public void setValue(final Object object) {
    this.selectedValues.clear();
    final List valueList = (List)object;
    super.setValue(valueList);
    if (valueList != null) {
      for (final Iterator values = valueList.iterator(); values.hasNext();) {
        final Object value = values.next();
        final FieldValue option = (FieldValue)this.optionValueMap.get(value);
        if (option != null) {
          this.selectedValues.put(option.getStringValue(), option.getValue());
        }
      }
    }
  }
}
