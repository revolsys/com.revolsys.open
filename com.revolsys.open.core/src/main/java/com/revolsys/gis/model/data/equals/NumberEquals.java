package com.revolsys.gis.model.data.equals;

import java.util.Collection;

public class NumberEquals implements Equals<Object> {
  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      return (object2 == null);
    } else if (object2 == null) {
      return false;
    } else if (object1 instanceof Number) {
      if (object2 instanceof Number) {
        return Double.compare(((Number)object1).doubleValue(),
          ((Number)object2).doubleValue()) == 0;
      } else {
        return Double.compare(((Number)object1).doubleValue(),
          Double.parseDouble(object2.toString())) == 0;
      }
    } else if (object2 instanceof Number) {
      return Double.compare(((Number)object2).doubleValue(),
        Double.parseDouble(object1.toString())) == 0;
    } else {
      return Double.compare(Double.parseDouble(object1.toString()),
        Double.parseDouble(object2.toString())) == 0;

    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
