package com.revolsys.converter.string;

import com.revolsys.util.MathUtil;

public abstract class AbstractNumberStringConverter<T> implements StringConverter<T> {
  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return MathUtil.toString(number);
    } else {
      return value.toString();
    }
  }
}
