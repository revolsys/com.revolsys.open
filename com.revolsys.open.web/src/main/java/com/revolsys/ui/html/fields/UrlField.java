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

public class UrlField extends TextField {
  public UrlField(final String name, final boolean required) {
    super(name, 40, 1000, required);
  }

  public UrlField(final String name, final String defaultValue,
    final boolean required) {
    super(name, 40, 1000, defaultValue, required);
  }

  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      int length = getInputValue().length();
      if (length > getMaxLength()) {
        setInputValue(getInputValue().substring(0, getMaxLength()));
        addValidationError("Cannot exceed " + getMaxLength() + " characters");
        valid = false;
      } else if (length < getMinLength()) {
        addValidationError("Must be at least " + getMinLength() + " characters");
        valid = false;
        // } else if (!GenericValidator.isUrl(inputValue)) {
        // addValidationError("Enter a valid URL");
        // valid = false;
      }
    }
    if (valid) {
      setValue(getInputValue());
    }
    return valid;
  }
}
