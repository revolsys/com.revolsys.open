package com.revolsys.ui.html.fields;

import java.math.BigDecimal;

import com.revolsys.util.Property;

public class BigDecimalField extends NumberField {

  private int scale;

  public BigDecimalField() {
    this(null, false);
  }

  public BigDecimalField(final String name, final boolean required) {
    super(name, 20, 50, null, required);
  }

  public BigDecimalField(final String name, final boolean required, final Object defaultValue) {
    super(name, 20, 50, defaultValue, required);
  }

  public BigDecimalField(final String name, final int scale, final boolean required) {
    super(name, 20, 50, null, required);
    this.scale = scale;
  }

  @Override
  public Number getNumber(final String value) {
    return new BigDecimal(value);
  }

  /**
   * @return Returns the scale.
   */
  public final int getScale() {
    return this.scale;
  }

  public void setScale(final int scale) {
    this.scale = scale;
  }

  @Override
  public void setTextValue(final String value) {
    if (Property.hasValue(value)) {
      super.setTextValue(value);
      final BigDecimal numericValue = getValue();
      if (numericValue.scale() > this.scale) {
        throw new IllegalArgumentException("Scale must be <= " + this.scale);
      }
    } else {
      setValue(null);
    }
  }
}
