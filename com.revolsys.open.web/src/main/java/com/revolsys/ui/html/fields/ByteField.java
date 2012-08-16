package com.revolsys.ui.html.fields;

public class ByteField extends NumberField {
  public ByteField() {
    this(null, false);
  }

  public ByteField(final String name, final boolean required) {
    this(name, required, null);
  }

  public ByteField(final String name, final boolean required,
    Object defaultValue) {
    super(name, 3, 3, defaultValue, required, Byte.MIN_VALUE, Byte.MAX_VALUE);
    setCssClass("digits");
  }

  @Override
  public Number getNumber(String value) {
    return Byte.valueOf(value);
  }

}
