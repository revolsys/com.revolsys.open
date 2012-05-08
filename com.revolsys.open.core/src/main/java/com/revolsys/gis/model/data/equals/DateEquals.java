package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Date;

public class DateEquals implements Equals<Date> {
  @Override
  public boolean equals(Date object1, Date object2, Collection<String> exclude) {
    if (object1.compareTo(object2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void setEqualsRegistry(EqualsRegistry equalsRegistry) {
  }
}
