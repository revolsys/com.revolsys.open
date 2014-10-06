/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

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
package com.revolsys.data.validator;

import java.math.BigDecimal;

import com.revolsys.data.record.schema.FieldDefinition;

public class BigDecimalAttributeValidator implements AttributeValueValidator {
  private boolean decimal = true;

  private BigDecimal maxValue;

  private BigDecimal minValue;

  public BigDecimalAttributeValidator() {
  }

  public BigDecimalAttributeValidator(final BigDecimal minValue,
    final BigDecimal maxValue, final boolean decimal) {
    this.minValue = new BigDecimal(String.valueOf(minValue));
    this.maxValue = new BigDecimal(String.valueOf(maxValue));
    this.decimal = decimal;
  }

  public BigDecimalAttributeValidator(final boolean decimal) {
    this.decimal = decimal;
  }

  public BigDecimalAttributeValidator(final double minValue,
    final double maxValue) {
    this.minValue = new BigDecimal(String.valueOf(minValue));
    this.maxValue = new BigDecimal(String.valueOf(maxValue));
    decimal = true;
  }

  public BigDecimalAttributeValidator(final long minValue, final long maxValue) {
    this.minValue = new BigDecimal(String.valueOf(minValue));
    this.maxValue = new BigDecimal(String.valueOf(maxValue));
    decimal = false;
  }

  @Override
  public boolean isValid(final FieldDefinition attributeDefinition, final Object value) {
    if (value instanceof BigDecimal) {
      final BigDecimal number = (BigDecimal)value;
      if (!decimal && number.scale() > 0) {
        return false;
      } else if (minValue != null && number.compareTo(minValue) < 0) {
        return false;
      } else if (maxValue != null && number.compareTo(maxValue) > 0) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

}
