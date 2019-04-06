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
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;

public class PercentField extends BigDecimalField {
  public PercentField(final String name, final boolean required) {
    super(name, 2, required);
    setSize(6);
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    String inputValue = request.getParameter(getName());
    if (inputValue == null) {
      final BigDecimal decimal = (BigDecimal)getInitialValue(request);
      setValue(decimal);
      if (getValue() != null) {
        setValue(
          decimal.multiply(new BigDecimal(100)).setScale(getScale(), BigDecimal.ROUND_HALF_UP));
        final DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(getScale());
        inputValue = format.format(getValue());
      }
    }
    setInputValue(inputValue);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    super.serializeElement(out);
    out.write(" %");
  }

  @Override
  public void setTextValue(final String value) {
    super.setTextValue(value);
    final BigDecimal percent = getValue();
    if (percent.scale() > getScale()) {
      throw new IllegalArgumentException("Scale must be <= " + getScale());
    } else if (percent.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException("Must be <= 100 %");
    } else {
      setValue(percent.divide(new BigDecimal("100"), 3, BigDecimal.ROUND_HALF_UP));
    }
  }
}
