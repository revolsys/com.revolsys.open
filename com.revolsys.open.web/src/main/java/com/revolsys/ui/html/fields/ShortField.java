package com.revolsys.ui.html.fields;

public class ShortField extends NumberField {
  public ShortField() {
    this(null, false);
  }

  public ShortField(final String name, final boolean required) {
    this(name, required, null);
  }

  public ShortField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 10, 10, defaultValue, required, Short.MIN_VALUE, Short.MAX_VALUE);
    setCssClass("digits");
  }

  @Override
  public Number getNumber(String value) {
    return Short.valueOf(value);
  }

}
