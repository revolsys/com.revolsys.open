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

import org.springframework.util.StringUtils;

public class IntegerField extends TextField {

  private int minimumValue = Integer.MIN_VALUE;

  private int maximumValue = Integer.MAX_VALUE;

  public IntegerField(final String name, final boolean required) {
    super(name, 10, required);
  }

  /**
   * @return Returns the maximumValue.
   */
  public int getMaximumValue() {
    return maximumValue;
  }

  /**
   * @return Returns the minimumValue.
   */
  public int getMinimumValue() {
    return minimumValue;
  }

  /**
   * @param maximumValue The maximumValue to set.
   */
  public void setMaximumValue(final int maximumValue) {
    this.maximumValue = maximumValue;
  }

  /**
   * @param minimumValue The minimumValue to set.
   */
  public void setMinimumValue(final int minimumValue) {
    this.minimumValue = minimumValue;
  }

  @Override
  public void setTextValue(final String value) {
    super.setTextValue(value);
    if (StringUtils.hasLength(value)) {
      try {
        final Integer intValue = new Integer(value);
        if (intValue.intValue() < minimumValue) {
          throw new IllegalArgumentException("Must be >= " + minimumValue);
        } else if (intValue.intValue() > maximumValue) {
          throw new IllegalArgumentException("Must be <= " + maximumValue);
        } else {
          setValue(intValue);
        }
      } catch (final NumberFormatException e) {
        throw new IllegalArgumentException("Must be a valid number");
      }
    } else {
      super.setValue(null);
    }
  }
}
