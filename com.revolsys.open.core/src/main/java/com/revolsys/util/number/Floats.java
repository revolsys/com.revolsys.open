package com.revolsys.util.number;

import com.revolsys.datatype.AbstractDataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.util.Property;

public class Floats extends AbstractDataType {
  public static float add(final float left, final Number right) {
    return left + right.floatValue();
  }

  public static float divide(final float left, final Number right) {
    return left / right.floatValue();
  }

  public static boolean equal(final float number1, final float number2) {
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

  public static boolean equal(final Object number1, final Object number2) {
    return equal((float)number1, (float)number2);
  }

  public static float mod(final float left, final Number right) {
    return left % right.floatValue();
  }

  public static float multiply(final float left, final Number right) {
    return left * right.floatValue();
  }

  public static float subtract(final float left, final Number right) {
    return left - right.floatValue();
  }

  /**
   * Convert the value to a Float. If the value cannot be converted to a number
   * an exception is thrown
   */
  public static Float toValid(final Object value) {
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
  public static Float toValid(final String string) {
    if (Property.hasValue(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }

  public Floats() {
    super("float", Float.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return equal((float)value1, (float)value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    return Float.valueOf(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return toString((float)value);
  }
}
