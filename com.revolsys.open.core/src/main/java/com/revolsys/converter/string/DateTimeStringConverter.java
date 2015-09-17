package com.revolsys.converter.string;

import java.util.Date;

import com.revolsys.util.DateUtil;

public class DateTimeStringConverter implements StringConverter<Date> {
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
    return DateUtil.getDate(value);
  }

  @Override
  public Date toObject(final String string) {
    return DateUtil.getDate(string);
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", date);
    } else {
      return value.toString();
    }
  }
}
