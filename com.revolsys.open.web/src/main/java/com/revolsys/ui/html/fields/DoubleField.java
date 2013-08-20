package com.revolsys.ui.html.fields;

public class DoubleField extends NumberField {
  public DoubleField() {
    this(null, false);
  }

  public DoubleField(final String name, final boolean required) {
    this(name, required, null);
  }

  public DoubleField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 22, 22, defaultValue, required, Double.MIN_VALUE, Double.MAX_VALUE);
  }

  @Override
  public Number getNumber(String value) {
    return Double.valueOf(value);
  }

}
