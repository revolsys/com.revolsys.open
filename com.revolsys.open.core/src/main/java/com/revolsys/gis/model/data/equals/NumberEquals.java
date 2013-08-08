package com.revolsys.gis.model.data.equals;

import java.util.Collection;

import com.revolsys.converter.string.StringConverterRegistry;

public class NumberEquals implements Equals<Object> {
  private boolean equal(final double number1, final double number2) {
    if (Double.isNaN(number1)) {
      return Double.isNaN(number2);
    } else if (Double.isInfinite(number1)) {
      return Double.isInfinite(number2);
    } else {
      return Double.compare(number1, number2) == 0;
    }
  }

  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    try {
      if (object1 == null) {
        return (object2 == null);
      } else if (object2 == null) {
        return false;
      } else {
        final double number1 = StringConverterRegistry.toObject(Double.class,
          object1);
        final double number2 = StringConverterRegistry.toObject(Double.class,
          object2);
        return equal(number1, number2);
      }
    } catch (final Throwable e) {
      return false;
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
