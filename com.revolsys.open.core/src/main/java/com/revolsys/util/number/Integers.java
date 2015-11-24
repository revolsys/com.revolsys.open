package com.revolsys.util.number;

public interface Integers {
  /**
   * Convert the value to a Integer. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Integer toInteger(final Object value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  /**
   * Convert the value to a Integer. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Integer toInteger(final String value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  /**
   * Convert the value to a Integer. If the value cannot be converted to a number
   * an exception is thrown
   */
  static Integer toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number and exception is thrown.
   */
  static Integer toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      int limit = -Integer.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Integer.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid int");
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        int result = 0;
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
          return -result;
        }
      }
    }
  }

  default int add(final int left, final Number right) {
    return left + right.intValue();
  }

  default int divide(final int left, final Number right) {
    return left / right.intValue();
  }

  default int mod(final int left, final Number right) {
    return left % right.intValue();
  }

  default int multiply(final int left, final Number right) {
    return left * right.intValue();
  }

  default int subtract(final int left, final Number right) {
    return left - right.intValue();
  }
}
