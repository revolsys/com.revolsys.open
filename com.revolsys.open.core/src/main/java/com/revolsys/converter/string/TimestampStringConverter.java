package com.revolsys.converter.string;

import java.sql.Timestamp;
import java.util.Date;

import com.revolsys.util.DateUtil;
import com.revolsys.util.Property;

public class TimestampStringConverter implements StringConverter<Timestamp> {
  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

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
  public Timestamp toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Timestamp) {
      final Timestamp timestamp = (Timestamp)value;
      return timestamp;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      final long time = date.getTime();
      return new Timestamp(time);
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Timestamp toObject(final String string) {
    if (Property.hasValue(string)) {
      return DateUtil.getTimestamp(string);
    } else {
      return null;
    }
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Timestamp) {
      return value.toString();
    } else {
      try {
        final Timestamp timestamp = toObject(value);
        return timestamp.toString();
      } catch (final Throwable t) {
        return value.toString();
      }
    }
  }
}
