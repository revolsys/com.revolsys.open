package com.revolsys.ui.html.fields;

public class LongField extends NumberField {
  public LongField() {
    this(null, false);
  }

  public LongField(final String name, final boolean required) {
    this(name, required, null);
  }

  public LongField(final String name, final boolean required, final Object defaultValue) {
    super(name, 23, 20, defaultValue, required, Long.MIN_VALUE, Long.MAX_VALUE);
    setCssClass("digits");
  }

  @Override
  public Number getNumber(final String value) {
    return Long.valueOf(value);
  }

}
