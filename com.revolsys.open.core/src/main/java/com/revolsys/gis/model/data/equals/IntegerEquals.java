package com.revolsys.gis.model.data.equals;

import java.util.Collection;

public class IntegerEquals extends AbstractEquals<Number> {
  protected boolean equalsNotNull(
    Number object1,
    Number object2,
    Collection<String> exclude) {
    final boolean equal = Math.abs(object1.doubleValue()
      - object2.doubleValue()) < 1.0;
    return equal;
  }
}
