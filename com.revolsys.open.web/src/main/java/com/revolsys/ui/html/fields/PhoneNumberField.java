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
import com.revolsys.ui.html.domain.PhoneNumber;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

/**
 * @author paustin
 * @version 1.0
 */
public class PhoneNumberField extends Field {
  public static final int FIELD_MAX_LENGTH = 20;

  public static final int FIELD_SIZE = 20;

  public static final int VALUE_MAX_LENGTH = 12;

  public static final int VALUE_MIN_LENGTH = 10;

  private String inputValue = "";

  public PhoneNumberField(final boolean required) {
    this("phone", required);
  }

  public PhoneNumberField(final String name, final boolean required) {
    super(name, required);
  }

  public String getInputValue() {
    return this.inputValue;
  }

  @Override
  public boolean hasValue() {
    return this.inputValue != null && !this.inputValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.inputValue = request.getParameter(getName());
    if (this.inputValue == null) {
      setValue(getInitialValue(request));
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      final String phoneValue = PhoneNumber.normalize(this.inputValue);
      final int length = phoneValue.length();
      if (length > VALUE_MAX_LENGTH) {
        addValidationError("Cannot exceed " + VALUE_MAX_LENGTH + " characters");
        valid = false;
      } else if (length < VALUE_MIN_LENGTH) {
        addValidationError("Must be at least " + VALUE_MIN_LENGTH + " characters");
        valid = false;
      } else if (!PhoneNumber.isValid(phoneValue)) {
        addValidationError("Phone number must only contain characters [0-9()+- ]");
        valid = false;
      } else {
        this.inputValue = phoneValue;
        setValue(phoneValue);
      }
    } else {
      setValue(null);
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.TYPE, "tel");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    out.attribute(HtmlAttr.SIZE, Integer.toString(FIELD_SIZE));
    out.attribute(HtmlAttr.MAX_LENGTH, Integer.toString(FIELD_MAX_LENGTH));
    if (this.inputValue != null) {
      out.attribute(HtmlAttr.VALUE, this.inputValue);
    }
    out.endTag(HtmlElem.INPUT);
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      this.inputValue = PhoneNumber.format(value.toString());
    } else {
      this.inputValue = null;
    }
  }
}
