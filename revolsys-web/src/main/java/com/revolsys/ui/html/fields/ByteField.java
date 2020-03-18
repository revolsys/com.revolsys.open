package com.revolsys.ui.html.fields;

public class ByteField extends NumberField {
  public ByteField() {
    this(null, false);
  }

  public ByteField(final String name, final boolean required) {
    this(name, required, null);
  }

  public ByteField(final String name, final boolean required, final Object defaultValue) {
    super(name, 4, 4, defaultValue, required, Byte.MIN_VALUE, Byte.MAX_VALUE);
    setCssClass("byte");
  }

  @Override
  public Number getNumber(final String value) {
    return Byte.valueOf(value);
  }

}
