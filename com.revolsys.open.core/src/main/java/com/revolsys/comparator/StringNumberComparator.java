package com.revolsys.comparator;

import java.math.BigDecimal;
import java.util.Comparator;

public class StringNumberComparator implements Comparator<String> {
  @Override
  public int compare(final String string1, final String string2) {

    final BigDecimal number1 = new BigDecimal(string1.toUpperCase().replaceAll("[^0-9\\.\\-]", ""));
    final BigDecimal number2 = new BigDecimal(string2.toUpperCase().replaceAll("[^0-9\\.\\-]", ""));
    final int compare = number1.compareTo(number2);
    if (compare == 0) {
      return string1.compareTo(string2);
    } else {
      return compare;
    }
  }
}
