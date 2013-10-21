package com.revolsys.comparator;

import java.math.BigDecimal;
import java.util.Comparator;

import com.revolsys.converter.string.StringConverterRegistry;

public class NumericComparator<T> implements Comparator<T> {

  public static int numericCompare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (value2 == null) {
      return 1;
    } else {
      final BigDecimal number1 = StringConverterRegistry.toObject(
        BigDecimal.class, value1);
      final BigDecimal number2 = StringConverterRegistry.toObject(
        BigDecimal.class, value2);
      return number1.compareTo(number2);
    }
  }

  @Override
  public int compare(final T value1, final T value2) {
    return numericCompare(value1, value2);
  }
}
