package com.revolsys.collection.range;

import com.revolsys.beans.Classes;
import com.revolsys.util.Numbers;
import com.revolsys.util.Property;

public class Ranges {
  public static AbstractRange<?> create(final char value) {
    if (Numbers.isDigit(value)) {
      return new IntRange(value - '0');
    } else if (CharRange.isLowerOrUpper(value)) {
      return new CharRange(value);
    } else {
      return new StringSingletonRange(value);
    }
  }

  public static AbstractRange<?> create(final char from, final char to) {
    if (Numbers.isDigit(from) && Numbers.isDigit(to)) {
      return new IntRange(from - '0', to - '0');
    } else {
      return new CharRange(from, to);
    }
  }

  public static AbstractRange<?> create(final int value) {
    return new IntRange(value);
  }

  public static AbstractRange<?> create(final int from, final int to) {
    return new IntRange(from, to);
  }

  public static AbstractRange<?> create(final long value) {
    return new LongRange(value);
  }

  public static AbstractRange<?> create(final long from, final long to) {
    return new LongRange(from, to);
  }

  public static AbstractRange<?> create(Object value) {
    value = toValue(value);
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      return create(((Long)value).longValue());
    } else if (Numbers.isPrimitiveIntegral(value)) {
      return create(((Number)value).intValue());
    } else if (value instanceof Character) {
      final Character character = (Character)value;
      return create(character.charValue());
    } else {
      return new StringSingletonRange(value.toString());
    }
  }

  public static AbstractRange<?> create(final Object from, final Object to) {
    final Object fromValue = toValue(from);
    final Object toValue = toValue(to);
    if (fromValue == null) {
      return create(toValue);
    }
    if (fromValue instanceof Long) {
      final long fromLong = (Long)fromValue;
      if (toValue instanceof Long) {
        final long toLong = (Long)toValue;
        if (fromLong != 0 && from.toString().charAt(0) == '0'
          || toLong != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromLong, toLong);
        } else {
          return create(fromLong, toLong);
        }
      } else if (toValue instanceof Integer) {
        final long toLong = (Integer)toValue;
        if (fromLong != 0 && from.toString().charAt(0) == '0'
          || toLong != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromLong, toLong);
        } else {
          return create(fromLong, toLong);
        }
      } else {
        throw new RangeInvalidException("Cannot create range from " + fromValue + " (Long) and "
          + toValue + " (" + Classes.className(toValue.getClass()) + ")");
      }
    } else if (fromValue instanceof Integer) {
      final long fromInt = (Integer)fromValue;
      if (toValue instanceof Long) {
        final long toLong = (Long)toValue;
        if (fromInt != 0 && from.toString().charAt(0) == '0'
          || toLong != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromInt, toLong);
        } else {
          return create(fromInt, toLong);
        }
      } else if (toValue instanceof Integer) {
        final long toInt = (Integer)toValue;
        if (fromInt != 0 && from.toString().charAt(0) == '0'
          || toInt != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromInt, toInt);
        } else {
          return create(fromInt, toInt);
        }
      } else {
        throw new RangeInvalidException("Cannot create range from " + fromValue + " (Long) and "
          + toValue + " (" + Classes.className(toValue.getClass()) + ")");
      }
    } else if (fromValue instanceof Character) {
      final char fromChar = (Character)fromValue;
      if (toValue instanceof Character) {
        final char toChar = (Character)toValue;
        return create(fromChar, toChar);
      } else {
        throw new RangeInvalidException("Cannot create range from " + fromValue
          + " (Character) and " + toValue + " (" + Classes.className(toValue.getClass()) + ")");
      }
    } else {
      throw new RangeInvalidException("Cannot create range from " + fromValue + " (String) and "
        + toValue + " (" + Classes.className(toValue.getClass()) + ")");
    }
  }

  private static boolean isNumeric(final RangeSet rangeSet) {
    if (rangeSet == null) {
      return false;
    } else {
      for (final AbstractRange<?> range : rangeSet.getRanges()) {
        if (range instanceof LongRange) {
        } else if (range instanceof IntRange) {
        } else if (range instanceof LongPaddedRange) {
        } else if (range instanceof CrossProductRange) {
          final CrossProductRange crossProduct = (CrossProductRange)range;
          for (final AbstractRange<?> subRange : crossProduct.getRanges()) {
            if (subRange instanceof LongRange) {
            } else if (subRange instanceof IntRange) {
            } else if (subRange instanceof LongPaddedRange) {
            } else {
              return false;
            }
          }
        } else {
          return false;
        }
      }
      return true;
    }
  }

  public static boolean isNumeric(final String rangeSpec) {
    try {
      final RangeSet rangeSet = RangeSet.create(rangeSpec);
      return isNumeric(rangeSet);
    } catch (final Throwable e) {
      return false;
    }
  }

  public static Object toValue(final Object value) {
    if (value == null) {
      return null;
    } else if (Numbers.isPrimitiveIntegral(value)) {
      final Number number = (Number)value;
      final long longValue = number.longValue();
      final int intValue = (int)longValue;
      if (intValue == longValue) {
        return intValue;
      } else {
        return longValue;
      }
    } else if (value instanceof Character) {
      final Character character = (Character)value;
      return character.charValue();
    } else {
      return toValue(value.toString());
    }
  }

  public static Object toValue(final String value) {
    if (Property.hasValue(value)) {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        if (value.length() == 1) {
          final char character = value.charAt(0);
          if (CharRange.isLowerOrUpper(character)) {
            return character;
          }
        }
        return value;
      } else {
        return longValue;
      }
    } else {
      return null;
    }
  }
}
