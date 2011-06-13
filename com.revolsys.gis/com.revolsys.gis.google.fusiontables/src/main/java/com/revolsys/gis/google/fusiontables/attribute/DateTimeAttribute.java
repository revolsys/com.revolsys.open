package com.revolsys.gis.google.fusiontables.attribute;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.revolsys.gis.data.model.types.DataTypes;

public class DateTimeAttribute extends FusionTablesAttribute {
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
    "yyyy-MM-dd");

  public static void appendString(final StringBuffer sql, final Object object) {
    if (object == null) {
      sql.append('\'');
      sql.append('\'');
    } else {
      String string;
      if (object instanceof Date) {
        string = DATE_FORMAT.format(object);
      } else {
        string = object.toString();
        try {
          final Date date = DATE_FORMAT.parse(string);
          string = DATE_FORMAT.format(date);
        } catch (final ParseException e) {
          throw new IllegalArgumentException("Expecting a YYYY-MM-DD date");
        }
      }
      sql.append('\'');
      sql.append(string);
      sql.append('\'');
    }
  }

  public DateTimeAttribute(final String name) {
    super(name, DataTypes.DATE_TIME);
  }

  @Override
  public void appendValue(final StringBuffer sql, final Object object) {
    appendString(sql, object);
  }

  @Override
  public DateTimeAttribute clone() {
    return new DateTimeAttribute(getName());
  }

  @Override
  public Object parseString(final String string) {
    if (string.trim().length() == 0) {
      return null;
    } else {
      try {
        return DATE_FORMAT.parse(string);
      } catch (final ParseException e) {
        throw new IllegalArgumentException("Expecting a YYYY-MM-DD date");
      }
    }
  }
}
