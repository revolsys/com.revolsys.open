package com.revolsys.equals;

import java.util.Collection;
import java.util.Date;

import com.revolsys.util.Dates;

public class DateTimeEquals implements Equals<Object> {
  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      return object2 == null;
    } else if (object2 == null) {
      return false;
    } else {
      final Date date1 = Dates.getDate(object1);
      final Date date2 = Dates.getDate(object2);
      if (date1.compareTo(date2) == 0) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
