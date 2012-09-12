package com.revolsys.gis.model.data.equals;

import java.util.Collection;

public class IntegerEquals extends AbstractEquals<Number> {
  @Override
  protected boolean equalsNotNull(final Number object1, final Number object2,
    final Collection<String> exclude) {
    final boolean equal = Math.abs(object1.doubleValue()
      - object2.doubleValue()) < 1.0;
    return equal;
  }
}
