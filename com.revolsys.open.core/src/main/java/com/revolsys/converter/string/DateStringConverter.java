package com.revolsys.converter.string;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.util.StringUtils;

public class DateStringConverter implements StringConverter<Date> {
  private DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  public DateStringConverter() {
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
    } else if (value instanceof java.util.Date) {
      final java.util.Date date = (java.util.Date)value;
      return new Date(date.getTime());
    } else  {
      return toObject(value.toString());
    }
  }

  @Override
  public Date toObject(final String string) {
    if (StringUtils.hasText(string)) {
      try {
        return new Date(FORMAT.parse(string).getTime());
      } catch (ParseException e) {
       throw new IllegalArgumentException("Date must be YYYY-MM-DD " + string, e);
      }
    } else {
      return null;
    }
  }
  
  @Override
  public String toString(Object value) {
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
