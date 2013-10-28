package com.revolsys.converter.string;

import java.sql.Date;

import org.springframework.util.StringUtils;

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
  public Date toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return date;
    } else if (value instanceof java.util.Date) {
      final java.util.Date date = (java.util.Date)value;
      return new Date(date.getTime());
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Date toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return new Date(DateUtil.parse("yyyy-MM-dd", string).getTime());
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
      return DateUtil.format("yyyy-MM-dd", date);
    } else {
      return value.toString();
    }
  }
}
