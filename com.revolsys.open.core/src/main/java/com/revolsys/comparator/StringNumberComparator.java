package com.revolsys.comparator;

import java.math.BigDecimal;
import java.util.Comparator;

import com.revolsys.util.number.BigDecimals;

public class StringNumberComparator implements Comparator<String> {
  @Override
  public int compare(final String string1, final String string2) {
    if (BigDecimals.isNumber(string1) && BigDecimals.isNumber(string2)) {
      final BigDecimal number1 = BigDecimals.toValid(string1);
      final BigDecimal number2 = BigDecimals.toValid(string2);
      return number1.compareTo(number2);
    } else {
      return string1.compareTo(string2);
    }
  }
}
