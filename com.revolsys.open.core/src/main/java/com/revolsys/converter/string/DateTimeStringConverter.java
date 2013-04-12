package com.revolsys.converter.string;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

public class DateTimeStringConverter implements StringConverter<Date> {
  private final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public DateTimeStringConverter() {
  }

  @Override
  public Class<Date> getConvertedClass() {
    return Date.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Date toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return date;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Date toObject(final String string) {
    if (StringUtils.hasText(string)) {
      try {
        return new Date(FORMAT.parse(string).getTime());
      } catch (final ParseException e) {
        throw new IllegalArgumentException("Date must be YYYY-MM-DD HH:mm:ss "
          + string, e);
      }
    } else {
      return null;
    }
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return FORMAT.format(date);
    } else {
      return value.toString();
    }
  }
}
