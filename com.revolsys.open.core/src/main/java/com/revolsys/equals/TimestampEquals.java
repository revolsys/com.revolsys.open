package com.revolsys.equals;

import java.sql.Timestamp;
import java.util.Collection;

import com.revolsys.util.Dates;

public class TimestampEquals implements Equals<Object> {
  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      return object2 == null;
    } else if (object2 == null) {
      return false;
    } else {
      final Timestamp date1 = Dates.getTimestamp(object1);
      final Timestamp date2 = Dates.getTimestamp(object2);
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
