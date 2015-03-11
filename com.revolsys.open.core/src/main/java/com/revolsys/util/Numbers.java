package com.revolsys.util;

public class Numbers {

  public static byte digitCount(long value) {
    if (value == 0) {
      return 1;
    } else if (value < 0) {
      value = -value;
    }
    if (value < 10) {
      return 1;
    } else if (value < 100) {
      return 2;
    } else if (value < 1000) {
      return 3;
    } else if (value < 10000) {
      return 4;
    } else if (value < 100000) {
      return 5;
    } else if (value < 1000000) {
      return 6;
    } else if (value < 10000000) {
      return 7;
    } else if (value < 100000000) {
      return 8;
    } else if (value < 1000000000) {
      return 9;
    } else if (value < 10000000000L) {
      return 10;
    } else if (value < 100000000000L) {
      return 11;
    } else if (value < 1000000000000L) {
      return 12;
    } else if (value < 10000000000000L) {
      return 13;
    } else if (value < 100000000000000L) {
      return 14;
    } else if (value < 1000000000000000L) {
      return 15;
    } else if (value < 10000000000000000L) {
      return 16;
    } else if (value < 100000000000000000L) {
      return 17;
    } else if (value < 1000000000000000000L) {
      return 18;
    } else {
      return 19;
    }
  }

  public static boolean isDigit(final char character) {
    if (character >= '0' && character <= '9') {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isDigit(final Character character) {
    if (character == null) {
      return false;
    } else {
      return isDigit(character.charValue());
    }
  }

  public static boolean isLong(final String part) {
    return toLong(part) != null;
  }

  public static boolean isPrimitive(final Object object) {
    if (object instanceof Integer) {
      return true;
    } else if (object instanceof Long) {
      return true;
    } else if (object instanceof Short) {
      return true;
    } else if (object instanceof Byte) {
      return true;
    } else if (object instanceof Double) {
      return true;
    } else if (object instanceof Float) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isPrimitiveDecimal(final Object object) {
    if (object instanceof Double) {
      return true;
    } else if (object instanceof Float) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isPrimitiveIntegral(final Object object) {
    if (object instanceof Integer) {
      return true;
    } else if (object instanceof Long) {
      return true;
    } else if (object instanceof Short) {
      return true;
    } else if (object instanceof Byte) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Byte toByte(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.byteValue();
    } else {
      final String string = value.toString();
      return toByte(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Byte toByte(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      byte limit = -Byte.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Byte.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              return null;
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        byte result = 0;
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
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
          }
        }
        if (negative) {
          return result;
        } else {
          return (byte)-result;
        }
      }
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Integer toInt(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      final String string = value.toString();
      return toInt(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Integer toInt(final String string) {
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
              return null;
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
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
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

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Long toLong(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String string = value.toString();
      return toLong(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Long toLong(final String string) {
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
              return null;
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
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
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

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Short toShort(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.shortValue();
    } else {
      final String string = value.toString();
      return toShort(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  public static Short toShort(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      short limit = -Short.MAX_VALUE;

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
              return null;
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
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
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

  public static String toStringPadded(final long value, final int digitCount) {
    final String string = Long.toString(value);
    if (string.length() < digitCount) {
      final StringBuilder builder = new StringBuilder(digitCount);
      for (int i = string.length(); i < digitCount; i++) {
        builder.append('0');
      }
      builder.append(string);
      return builder.toString();
    } else {
      return string;
    }
  }
}
