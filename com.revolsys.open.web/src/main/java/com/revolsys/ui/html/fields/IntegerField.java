package com.revolsys.ui.html.fields;

public class IntegerField extends NumberField {
  public IntegerField() {
    this(null, false);
  }

  public IntegerField(final String name, final boolean required) {
    this(name, required, null);
  }

  public IntegerField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 10, 10, defaultValue, required, Integer.MIN_VALUE, Integer.MAX_VALUE);
    setCssClass("digits");
  }

  @Override
  public Number getNumber(String value) {
    return Integer.valueOf(value);
  }

}
