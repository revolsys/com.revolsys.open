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

public class FieldValue {
  private String label = "";

  private String stringValue = "";

  private Object value = null;

  public FieldValue(final Object value, final String stringValue, final String label) {
    this.value = value;
    if (stringValue != null) {
      this.stringValue = stringValue;
    }
    if (label != null) {
      this.label = label;

    }
  }

  /**
   * @return Returns the label.
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return Returns the stringValue.
   */
  public String getStringValue() {
    return this.stringValue;
  }

  /**
   * @return Returns the value.
   */
  public <T> T getValue() {
    return (T)this.value;
  }
}
