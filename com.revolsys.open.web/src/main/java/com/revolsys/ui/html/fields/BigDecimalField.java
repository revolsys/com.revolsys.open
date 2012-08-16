package com.revolsys.ui.html.fields;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class BigDecimalField extends NumberField {

  private int scale;

  public BigDecimalField() {
    this(null, false);
  }

  public BigDecimalField(final String name, final boolean required) {
    super(name, 20, 50, null, required);
  }

  public BigDecimalField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 20, 50, defaultValue, required);
  }

  public BigDecimalField(final String name, final int scale,
    final boolean required) {
    super(name, 20, 50, null, required);
    this.scale = scale;
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

  @Override
  public Number getNumber(String value) {
    return new BigDecimal(value);
  }

  @Override
  public void setTextValue(final String value) {
    if (StringUtils.hasLength(value)) {
      super.setTextValue(value);
      final BigDecimal numericValue = getValue();
      if ((numericValue).scale() > scale) {
        throw new IllegalArgumentException("Scale must be <= " + scale);
      }
    } else {
      setValue(null);
    }
  }
}
