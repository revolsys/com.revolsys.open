package com.revolsys.ui.html.fields;

public class FloatField extends NumberField {
  public FloatField() {
    this(null, false);
  }

  public FloatField(final String name, final boolean required) {
    this(name, required, null);
  }

  public FloatField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 18, 18, defaultValue, required, Float.MIN_VALUE,
      Float.MAX_VALUE);
  }

  @Override
  public Number getNumber(String value) {
    return Float.valueOf(value);
  }

}
