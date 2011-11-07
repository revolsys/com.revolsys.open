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

public abstract class Field extends Element {
  private String name = "";

  private List validationErrors = Collections.EMPTY_LIST;

  private Object value;

  private boolean required = false;

  private boolean readOnly = false;

  private Object initialValue;

  public Field() {
  }

  public Field(
    final String name,
    final boolean required) {
    this.name = name;
    this.required = required;
  }

  public void addValidationError(
    final String error) {
    if (!hasValidationErrors()) {
      validationErrors = new ArrayList();
    }
    validationErrors.add(error);
  }

  public boolean hasValidationErrors() {
    return validationErrors != Collections.EMPTY_LIST;
  }

  public boolean hasValue() {
    return value != null;
  }

  public <T> T getInitialValue(
    HttpServletRequest request) {
    T value = (T)getContainer().getInitialValue(this, request);
    if (value == null) {
      return (T)initialValue;
    } else {
      return value;
    }
  }

  public List getValidationErrors() {
    return validationErrors;
  }

  public boolean isRequired() {
    return required;
  }

  /**
   * @return Returns the readOnly.
   */
  public final boolean isReadOnly() {
    return readOnly;
  }

  public void setName(
    String name) {
    this.name = name;
  }

  public void setRequired(
    boolean required) {
    this.required = required;
  }

  /**
   * @param readOnly The readOnly to set.
   */
  public final void setReadOnly(
    final boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isValid() {
    if (isRequired() && !hasValue()) {
      addValidationError("Required");
      return false;
    }
    return true;
  }

  public abstract void initialize(
    Form form,
    HttpServletRequest request);

  public void postInit(
    final HttpServletRequest request) {
  }

  public String getName() {
    return name;
  }

  public void setValue(
    final Object value) {
    this.value = value;
  }

  public <T> T getValue() {
    return (T)value;
  }

  /**
   * @return Returns the initialValue.
   */
  public Object getInitialValue() {
    return initialValue;
  }

  /**
   * @param initialValue The initialValue to set.
   */
  public void setInitialValue(
    Object initialValue) {
    this.initialValue = initialValue;
  }

}
