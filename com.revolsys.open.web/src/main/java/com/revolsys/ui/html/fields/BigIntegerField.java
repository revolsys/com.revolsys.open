package com.revolsys.ui.html.fields;

import java.math.BigInteger;

public class BigIntegerField extends NumberField {
  public BigIntegerField() {
    this(null, false);
  }

  public BigIntegerField(final String name, final boolean required) {
    this(name, required, null);
  }

  public BigIntegerField(final String name, final boolean required, final Object defaultValue) {
    super(name, 10, 30, defaultValue, required);
    setCssClass("long");
  }

  @Override
  public Number getNumber(final String value) {
    return new BigInteger(value);
  }

}
