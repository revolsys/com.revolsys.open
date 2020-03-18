package com.revolsys.ui.html.fields;

public class IntegerField extends NumberField {
  public IntegerField() {
    this(null, false);
  }

  public IntegerField(final String name, final boolean required) {
    this(name, required, null);
  }

  public IntegerField(final String name, final boolean required, final Object defaultValue) {
    super(name, 12, 11, defaultValue, required, Integer.MIN_VALUE, Integer.MAX_VALUE);
    setCssClass("int");
  }

  @Override
  public Number getNumber(final String value) {
    return Integer.valueOf(value);
  }

}
