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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;

public abstract class Field extends Element {
  private String name = "";

  private List validationErrors = Collections.EMPTY_LIST;

  private Object value;

  private boolean required = false;

  private boolean readOnly = false;

  private Object initialValue;

  public Field() {
  }

  public Field(final String name, final boolean required) {
    this.name = name;
    this.required = required;
  }

  public void addValidationError(final String error) {
    if (!hasValidationErrors()) {
      validationErrors = new ArrayList();
    }
    validationErrors.add(error);
  }

  /**
   * @return Returns the initialValue.
   */
  public Object getInitialValue() {
    return initialValue;
  }

  public <T> T getInitialValue(final HttpServletRequest request) {
    final ElementContainer container = getContainer();
    if (container == null) {
      return (T)initialValue;
    } else {
      final T value = (T)container.getInitialValue(this, request);

      if (value == null) {
        return (T)initialValue;
      } else {
        return value;
      }
    }
  }

  public String getName() {
    return name;
  }

  public List getValidationErrors() {
    return validationErrors;
  }

  public <T> T getValue() {
    return (T)value;
  }

  public boolean hasValidationErrors() {
    return validationErrors != Collections.EMPTY_LIST;
  }

  public boolean hasValue() {
    return value != null;
  }

  public abstract void initialize(Form form, HttpServletRequest request);

  /**
   * @return Returns the readOnly.
   */
  public final boolean isReadOnly() {
    return readOnly;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isValid() {
    if (isRequired() && !hasValue()) {
      addValidationError("Required");
      return false;
    }
    return true;
  }

  public void postInit(final HttpServletRequest request) {
  }

  /**
   * @param initialValue The initialValue to set.
   */
  public void setInitialValue(final Object initialValue) {
    this.initialValue = initialValue;
  }

  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @param readOnly The readOnly to set.
   */
  public final void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRequired(final boolean required) {
    this.required = required;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

}
