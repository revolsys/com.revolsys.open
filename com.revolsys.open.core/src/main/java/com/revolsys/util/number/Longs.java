package com.revolsys.util.number;

public interface Longs {
  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * an exception is thrown
   */
  static Long toValid(final Object value) {
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
  static Long toValid(final String string) {
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

  default long add(final long left, final Number right) {
    return left + right.longValue();
  }

  default long divide(final long left, final Number right) {
    return left / right.longValue();
  }

  default long mod(final long left, final Number right) {
    return left % right.longValue();
  }

  default long multiply(final long left, final Number right) {
    return left * right.longValue();
  }

  default long subtract(final long left, final Number right) {
    return left - right.longValue();
  }
}
