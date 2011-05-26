package com.revolsys.gis.google.fusiontables.attribute;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.revolsys.gis.data.model.types.DataTypes;

public class DateTimeAttribute extends FusionTablesAttribute {
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
    "yyyy-MM-dd");

  public DateTimeAttribute(String name) {
    super(name, DataTypes.DATE_TIME);
  }

  public void appendValue(StringBuffer sql, Object object) {
    sql.append('\'');
    if (object != null) {
      if (object instanceof Date) {
        sql.append(DATE_FORMAT.format(object));
      } else {
        String string = object.toString();
        try {
          Date date = DATE_FORMAT.parse(string);
          sql.append(DATE_FORMAT.format(date));
        } catch (ParseException e) {
          throw new IllegalArgumentException("Expecting a YYYY-MM-DD date");
        }
      }
    }
    sql.append('\'');
  }

  public Object parseString(String string) {
    if (string.trim().length() == 0) {
      return null;
    } else {
      try {
        return DATE_FORMAT.parse(string);
      } catch (ParseException e) {
        throw new IllegalArgumentException("Expecting a YYYY-MM-DD date");
      }
    }
  }

  @Override
  public DateTimeAttribute clone() {
    return new DateTimeAttribute(getName());
  }
}
