package com.revolsys.collection.range;

import com.revolsys.util.Numbers;

public class Ranges {
  public static AbstractRange<?> create(final char value) {
    if (Numbers.isDigit(value)) {
      return new LongRange(value - '0');
    } else {
      return new CharRange(value);
    }
  }

  public static AbstractRange<?> create(final char from, final char to) {
    if (Numbers.isDigit(from) && Numbers.isDigit(to)) {
      return new LongRange(from - '0', to - '0');
    } else {
      return new CharRange(from, to);
    }
  }

  public static AbstractRange<?> create(final long value) {
    return new LongRange(value);
  }

  public static AbstractRange<?> create(final long from, final long to) {
    return new LongRange(from, to);
  }

  public static AbstractRange<?> create(final Object value) {
    if (value == null) {
      return null;
    } else if (Numbers.isPrimitiveIntegral(value)) {
      final Number number = (Number)value;
      return create(number.longValue());
    } else if (value instanceof Character) {
      final Character character = (Character)value;
      return create(character);
    } else {
      return create(value.toString());
    }
  }

  public static AbstractRange<?> create(final String value) {
    if (value == null) {
      return null;
    } else {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        if (value.length() == 1) {
          final char character = value.charAt(0);
          if (character >= 'A' && character <= 'Z') {
            return create(character);
          }
        }
        return new StringSingletonRange(value);
      } else {
        return create(longValue);
      }
    }
  }
}
