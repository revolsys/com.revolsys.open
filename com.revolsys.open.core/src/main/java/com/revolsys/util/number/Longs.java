package com.revolsys.util.number;

import com.revolsys.datatype.AbstractDataType;
import com.revolsys.datatype.DataTypes;

public class Longs extends AbstractDataType {
  public static long add(final long left, final Number right) {
    return left + right.longValue();
  }

  public static int compareDistance(final long x1, final long y1, final long x2, final long y2) {
    final double distance1 = Math.sqrt(x1 * x1 + y1 * y1);
    final double distance2 = Math.sqrt(x2 * x2 + y2 * y2);
    int compare = Double.compare(distance1, distance2);
    if (compare == 0) {
      compare = Long.compare(y1, y2);
      if (compare == 0) {
        compare = Long.compare(x1, x2);
      }
    }
    return compare;
  }

  public static long divide(final long left, final Number right) {
    return left / right.longValue();
  }

  public static long mod(final long left, final Number right) {
    return left % right.longValue();
  }

  public static long multiply(final long left, final Number right) {
    return left * right.longValue();
  }

  public static long subtract(final long left, final Number right) {
    return left - right.longValue();
  }

  public static String toString(final long number) {
    return String.valueOf(number);
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * an exception is thrown
   */
  public static Long toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number and exception is thrown.
   */
  public static Long toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      long limit = -Long.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Long.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid int");
            }
            index++;
          break;
        }
        final long multmin = limit / 10;
        long result = 0;
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
                throw new IllegalArgumentException(string + " is not a valid int");
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                throw new IllegalArgumentException(string + " is not a valid int");
              }
              result -= digit;
            break;
            default:
              throw new IllegalArgumentException(string + " is not a valid int");
          }
        }
        if (negative) {
          return result;
        } else {
          return (long)-result;
        }
      }
    }
  }

  public Longs() {
    super("long", Long.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return (long)value1 == (long)value2;
  }

  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    }
    final String string = DataTypes.toString(value);
    return Long.valueOf(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return String.valueOf((long)value);
  }

  public static long toLong(final int upperInt, final int lowerInt) {
    final long lower = lowerInt & 0xffffffffL;
    final long upper = upperInt & 0xffffffffL;
    final long l = upper << 32 | lower;
    return l;
  }

}
