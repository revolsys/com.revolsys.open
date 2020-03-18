package com.revolsys.ui.html.fields;

public class DoubleField extends NumberField {
  public DoubleField() {
    this(null, false);
  }

  public DoubleField(final String name, final boolean required) {
    this(name, required, null);
  }

  public DoubleField(final String name, final boolean required, final Object defaultValue) {
    super(name, 23, 20, defaultValue, required, -Double.MAX_VALUE, Double.MAX_VALUE);
  }

  @Override
  public Number getNumber(final String value) {
    return Double.valueOf(value);
  }

}
