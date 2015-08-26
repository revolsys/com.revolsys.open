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
package com.revolsys.ui.html.form;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.HiddenField;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.SetObject;
import com.revolsys.util.Property;

public class HtmlUiBuilderObjectForm extends Form {

  private final HtmlUiBuilder builder;

  private List<String> fieldKeys;

  private final Object object;

  private final String typeLabel;

  public HtmlUiBuilderObjectForm(final Object object, final HtmlUiBuilder uiBuilder,
    final List<String> fieldKeys) {
    super(uiBuilder.getTypeName());
    this.object = object;
    this.builder = uiBuilder;
    this.typeLabel = uiBuilder.getTitle();
    this.fieldKeys = fieldKeys;
  }

  public HtmlUiBuilderObjectForm(final Object object, final HtmlUiBuilder uiBuilder,
    final String formName, final List<String> fieldKeys) {
    super(formName);
    this.object = object;
    this.builder = uiBuilder;
    this.typeLabel = uiBuilder.getTitle();
    this.fieldKeys = fieldKeys;
    add(new HiddenField("htmlCss", false));
    add(new HiddenField("plain", false));
  }

  @Override
  public Object getInitialValue(final Field field, final HttpServletRequest request) {
    if (this.object != null) {
      final String propertyName = field.getName();
      if (propertyName != Form.FORM_TASK_PARAM) {
        try {
          return Property.get(this.object, propertyName);
        } catch (final IllegalArgumentException e) {
          return null;
        }
      }
    }
    return null;
  }

  public Object getObject() {
    return this.object;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    for (final String key : this.fieldKeys) {
      if (!getFieldNames().contains(key)) {
        final Element field = this.builder.getAttribute(request, key);
        if (field instanceof SetObject) {
          ((SetObject)field).setObject(this.object);
        }
        if (field != null) {
          if (!getElements().contains(field)) {
            final Decorator label = this.builder.getAttributeLabel(key, field);

            add(field, label);
          }
        }
      }
    }
    this.builder.initializeForm(this, request);
    super.initialize(request);
  }

  public void setFieldKeys(final List fieldKeys) {
    this.fieldKeys = fieldKeys;
  }

  @Override
  public boolean validate() {
    boolean valid = true;
    if (this.object != null) {
      for (final Field field : getFields().values()) {
        if (!field.hasValidationErrors() && !field.isReadOnly()) {
          final String propertyName = field.getName();
          if (propertyName != Form.FORM_TASK_PARAM && this.fieldKeys.contains(propertyName)) {
            final Object value = field.getValue();
            try {
              this.builder.setValue(this.object, propertyName, value);
            } catch (final IllegalArgumentException e) {
              field.addValidationError(e.getMessage());
              valid = false;
            }
          }
        }
      }
      if (valid) {
        valid &= this.builder.validateForm(this);
      }
    }
    valid &= super.validate();
    return valid;
  }
}
