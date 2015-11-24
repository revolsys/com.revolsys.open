package com.revolsys.util.number;

import com.revolsys.util.Property;

public interface Floats {
  /**
   * Convert the value to a Float. If the value cannot be converted to a number
   * an exception is thrown
   */
  static Float toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.floatValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Float. If the value cannot be converted to a number and exception is thrown.
   */
  static Float toValid(final String string) {
    if (Property.hasValue(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }

  default float add(final float left, final Number right) {
    return left + right.floatValue();
  }

  default float divide(final float left, final Number right) {
    return left / right.floatValue();
  }

  default float mod(final float left, final Number right) {
    return left % right.floatValue();
  }

  default float multiply(final float left, final Number right) {
    return left * right.floatValue();
  }

  default float subtract(final float left, final Number right) {
    return left - right.floatValue();
  }
}
