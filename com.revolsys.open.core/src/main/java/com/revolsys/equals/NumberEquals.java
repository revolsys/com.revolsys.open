package com.revolsys.equals;

import java.math.BigDecimal;
import java.util.Collection;

public class NumberEquals implements Equals<Object> {
  public static boolean equal(final double number1, final double number2) {
    if (Double.isNaN(number1)) {
      return Double.isNaN(number2);
    } else if (Double.isInfinite(number1)) {
      return Double.isInfinite(number2);
    } else {
      if (Double.compare(number1, number2) == 0) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    try {
      if (object1 == null) {
        return object2 == null;
      } else if (object2 == null) {
        return false;
      } else {
        final Double number1 = new BigDecimal(object1.toString()).doubleValue();
        final Double number2 = new BigDecimal(object2.toString()).doubleValue();
        final boolean equal = equal(number1, number2);
        if (equal) {
          return true;
        } else {
          return false;
        }
      }
    } catch (final Throwable e) {
      return false;
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
