package org.jeometry.common.number;

import org.jeometry.common.data.type.AbstractDataType;
import org.jeometry.common.data.type.DataTypes;

public class Shorts extends AbstractDataType {
  public static short add(final short left, final Number right) {
    return (short)(left + right.shortValue());
  }

  public static short divide(final short left, final Number right) {
    return (short)(left / right.shortValue());
  }

  public static short mod(final short left, final Number right) {
    return (short)(left % right.shortValue());
  }

  public static short multiply(final short left, final Number right) {
    return (short)(left * right.shortValue());
  }

  public static short subtract(final short left, final Number right) {
    return (short)(left - right.shortValue());
  }

  public static String toString(final short number) {
    return String.valueOf(number);
  }

  /**
   * Convert the value to a Short. If the value cannot be converted to a number
   * an exception is thrown
   */
  public static Short toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.shortValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Short. If the value cannot be converted to a number and exception is thrown.
   */
  public static Short toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      int limit = -Short.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Short.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid short");
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        short result = 0;
        for (; index < length; index++) {
          final char character = string.charAt(index);
          switch (character) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (result < multmin) {
                throw new IllegalArgumentException(string + " is not a valid short");
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                throw new IllegalArgumentException(string + " is not a valid short");
              }
              result -= digit;
            break;
            default:
              throw new IllegalArgumentException(string + " is not a valid short");
          }
        }
        if (negative) {
          return result;
        } else {
          return (short)-result;
        }
      }
    }
  }

  public Shorts() {
    super("short", Short.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return (short)value1 == (short)value2;
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    return toValid(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return String.valueOf((short)value);
  }
}
