package com.revolsys.ui.html.fields;

public class LongField extends NumberField {
  public LongField() {
    this(null, false);
  }

  public LongField(final String name, final boolean required) {
    this(name, required, null);
  }

  public LongField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 19, 19, defaultValue, required, Long.MIN_VALUE, Long.MAX_VALUE);
    setCssClass("digits");
  }

  @Override
  public Number getNumber(String value) {
    return Long.valueOf(value);
  }

}
