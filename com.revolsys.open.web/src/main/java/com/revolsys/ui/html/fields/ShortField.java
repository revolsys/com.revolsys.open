package com.revolsys.ui.html.fields;

public class ShortField extends NumberField {
  public ShortField() {
    this(null, false);
  }

  public ShortField(final String name, final boolean required) {
    this(name, required, null);
  }

  public ShortField(final String name, final boolean required, final Object defaultValue) {
    super(name, 6, 6, defaultValue, required, Short.MIN_VALUE, Short.MAX_VALUE);
    setCssClass("short");
  }

  @Override
  public Number getNumber(final String value) {
    return Short.valueOf(value);
  }

}
