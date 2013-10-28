package com.revolsys.converter.string;

import java.util.Date;

import org.springframework.util.StringUtils;

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
      return DateUtil.parse("yyyy-MM-dd HH:mm:ss", string);
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
      return DateUtil.format("yyyy-MM-dd HH:mm:ss", date);
    } else {
      return value.toString();
    }
  }
}
