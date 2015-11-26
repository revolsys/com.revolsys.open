package com.revolsys.util.number;

import com.revolsys.util.Property;

public interface Floats {
  static boolean equal(final float number1, final float number2) {
    if (Float.isNaN(number1)) {
      return Float.isNaN(number2);
    } else if (Float.isInfinite(number1)) {
      return Float.isInfinite(number2);
    } else {
      if (Float.compare(number1, number2) == 0) {
        return true;
      } else {
        return false;
      }
    }
  }

  static boolean equal(final Object number1, final Object number2) {
    return equal((float)number1, (float)number2);
  }

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
