package com.revolsys.io.saif.util;

import java.sql.Date;

public class DateConverter implements OsnConverter {
  public static final Date NULL_DATE = new Date(0, 0, 1);

  public Object read(
    final OsnIterator iterator) {
    int year = 0;
    int month = 0;
    int day = 1;
    boolean nullDate = false;
    String name = iterator.nextAttributeName();
    while (name != null) {

      if (name.equals("day")) {
        day = iterator.nextIntValue();
        if (day == 0) {
          day = 1;
        }
      } else if (name.equals("month")) {
        month = iterator.nextIntValue();
        if (month > 0) {
          month -= 1;
        }
      } else if (name.equals("year")) {
        year = iterator.nextIntValue();
        if (year > 0) {
          year -= 1900;
        } else {
          nullDate = true;
        }
      }
      name = iterator.nextAttributeName();
    }
    if (nullDate) {
      return NULL_DATE;
    } else {
      return new Date(year, month, day);
    }
  }

  public void write(
    final OsnSerializer serializer,
    final Object object) {
  }

}
