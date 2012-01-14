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

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class BigDecimalField extends TextField {

  private BigDecimal minimumValue;

  private BigDecimal maximumValue;

  private int scale;

  public BigDecimalField(final String name, final boolean required) {
    super(name, 10, required);
  }

  public BigDecimalField(final String name, final int scale,
    final boolean required) {
    super(name, 10, required);
    this.scale = scale;
  }

  @Override
  public void setTextValue(final String value) {
    if (StringUtils.hasLength(value)) {
      try {
        BigDecimal numericValue = new BigDecimal(value);
        if ((numericValue).scale() > scale) {
          throw new IllegalArgumentException("Scale must be <= " + scale);
        } else if (minimumValue != null
          && numericValue.compareTo(minimumValue) < 0) {
          throw new IllegalArgumentException("Must be >= " + minimumValue);
        } else if (maximumValue != null
          && numericValue.compareTo(maximumValue) > 0) {
          throw new IllegalArgumentException("Must be <= " + maximumValue);
        } else {
          setValue(numericValue);
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Must be a valid number");
      }
    } else {
      setValue(null);
    }
  }

  public BigDecimal getMaximumValue() {
    return maximumValue;
  }

  public void setMaximumValue(final BigDecimal maximumValue) {
    this.maximumValue = maximumValue;
  }

  public BigDecimal getMinimumValue() {
    return minimumValue;
  }

  public void setMinimumValue(final BigDecimal minimumValue) {
    this.minimumValue = minimumValue;
  }

  /**
   * @return Returns the scale.
   */
  public final int getScale() {
    return scale;
  }

  public void setScale(final int scale) {
    this.scale = scale;
  }
}
