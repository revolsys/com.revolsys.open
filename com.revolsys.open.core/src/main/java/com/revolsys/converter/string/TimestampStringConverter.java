package com.revolsys.converter.string;

import java.sql.Timestamp;

import com.revolsys.util.DateUtil;

public class TimestampStringConverter implements StringConverter<Timestamp> {
  public TimestampStringConverter() {
  }

  @Override
  public Class<Timestamp> getConvertedClass() {
    return Timestamp.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Timestamp objectToObject(final Object value) {
    return DateUtil.getTimestamp(value);
  }

  @Override
  public Timestamp stringToObject(final String string) {
    return DateUtil.getTimestamp(string);
  }

  @Override
  public String objectToString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Timestamp) {
      return value.toString();
    } else {
      try {
        final Timestamp timestamp = objectToObject(value);
        return timestamp.toString();
      } catch (final Throwable t) {
        return value.toString();
      }
    }
  }
}
