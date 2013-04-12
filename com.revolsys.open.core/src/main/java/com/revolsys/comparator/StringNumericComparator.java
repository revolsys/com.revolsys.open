package com.revolsys.comparator;

import java.math.BigDecimal;
import java.util.Comparator;

public class StringNumericComparator implements Comparator<Object> {

  @Override
  public int compare(final Object value1, final Object value2) {
    return new BigDecimal(value1.toString()).compareTo(new BigDecimal(
      value2.toString()));
  }
}
