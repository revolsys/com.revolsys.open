package org.jeometry.common.number;

import java.math.BigDecimal;

import org.jeometry.common.data.type.AbstractDataType;
import org.jeometry.common.data.type.DataTypes;

public class BigDecimals extends AbstractDataType {
  public static boolean equalsNotNull(final BigDecimal number1, final BigDecimal number2) {
    if (number1.compareTo(number2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isNumber(final Object value) {
    if (value == null) {
      return false;
    } else if (value instanceof Number) {
      return true;
    } else {
      try {
        toValid(value);
        return true;
      } catch (final Throwable t) {
        return false;
      }
    }
  }

  public static String toString(final BigDecimal number) {
    return number.toPlainString();
  }

  public static BigDecimal toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof BigDecimal) {
      final BigDecimal number = (BigDecimal)value;
      return number.stripTrailingZeros();
    } else {
      final String string = DataTypes.toString(value);
      return new BigDecimal(string).stripTrailingZeros();
    }
  }

  public BigDecimals() {
    super("decimal", BigDecimal.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return equalsNotNull((BigDecimal)value1, (BigDecimal)value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    final BigDecimal decimal = new BigDecimal(string);
    return decimal.stripTrailingZeros();
  }

  @Override
  protected String toStringDo(final Object value) {
    return ((BigDecimal)value).toPlainString();
  }
}
