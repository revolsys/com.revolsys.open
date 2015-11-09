package com.revolsys.converter.string;

import java.sql.Date;

import com.revolsys.util.DateUtil;

public class DateStringConverter implements StringConverter<Date> {
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
  public Date objectToObject(final Object value) {
    return DateUtil.getSqlDate(value);
  }

  @Override
  public Date stringToObject(final String string) {
    return DateUtil.getSqlDate(string);
  }

  @Override
  public String objectToString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return DateUtil.format("yyyy-MM-dd", date);
    } else {
      return value.toString();
    }
  }
}
