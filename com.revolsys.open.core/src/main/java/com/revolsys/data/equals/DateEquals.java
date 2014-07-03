package com.revolsys.data.equals;

import java.util.Collection;
import java.util.Date;

public class DateEquals implements Equals<Date> {
  @Override
  public boolean equals(final Date object1, final Date object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      return object2 == null;
    } else if (object2 == null) {
      return false;
    } else if (object1.compareTo(object2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
