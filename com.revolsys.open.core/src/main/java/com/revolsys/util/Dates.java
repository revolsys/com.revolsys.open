package com.revolsys.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.revolsys.datatype.DataTypes;

public interface Dates {
  String DATE_TIME_NANOS_PATTERN = "\\s*(\\d{4})-(\\d{2})-(\\d{2})(?:[\\sT]+(\\d{2})\\:(\\d{2})\\:(\\d{2})(?:\\.(\\d{1,9}))?)?\\s*";

  static boolean equalsNotNull(final Object date1, final Object date2) {
    return ((Date)date1).compareTo((Date)date2) == 0;
  }

  static String format(final DateFormat format, final Date date) {
    return format.format(date);
  }

  static String format(final int dateStyle, final int timeStyle, final Timestamp timestamp) {
    final DateFormat format = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
    return format(format, timestamp);
  }

  static String format(final String pattern) {
    return format(pattern, new Date(System.currentTimeMillis()));
  }

  static String format(final String pattern, final Calendar calendar) {
    if (calendar == null) {
      return null;
    } else {
      final Date date = calendar.getTime();
      return format(pattern, date);
    }
  }

  static String format(final String pattern, final Date date) {
    if (date == null) {
      return null;
    } else {
      final DateFormat format = new SimpleDateFormat(pattern);
      return format(format, date);
    }
  }

  static Calendar getCalendar(final String dateString) {
    if (Property.hasValue(dateString)) {
      final Pattern pattern = Pattern.compile(DATE_TIME_NANOS_PATTERN);
      final Matcher matcher = pattern.matcher(dateString);
      if (matcher.find()) {
        final int year = getInteger(matcher, 1, 0);
        final int month = getInteger(matcher, 2, 0) - 1;
        final int day = getInteger(matcher, 3, 0);
        final int hour = getInteger(matcher, 4, 0);
        final int minute = getInteger(matcher, 5, 0);
        final int second = getInteger(matcher, 6, 0);
        int millisecond = getInteger(matcher, 7, 0);
        final Calendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
        if (millisecond != 0) {
          BigDecimal number = new BigDecimal("0." + millisecond);
          number = number.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_DOWN);
          millisecond = number.intValue();
          calendar.set(Calendar.MILLISECOND, millisecond);
        }
        return calendar;
      }
      return null;
    } else {
      return null;
    }
  }

  static Date getDate() {
    return new Date(System.currentTimeMillis());
  }

  static Date getDate(final DateFormat format, final String dateString) {
    if (!Property.hasValue(dateString)) {
      return null;
    } else {
      try {
        return format.parse(dateString);
      } catch (final ParseException e) {
        if (format instanceof SimpleDateFormat) {
          final SimpleDateFormat simpleFormat = (SimpleDateFormat)format;
          throw new IllegalArgumentException("Invalid date '" + dateString
            + "'. Must match pattern '" + simpleFormat.toPattern() + "'.", e);
        } else {
          throw new IllegalArgumentException("Invalid date  '" + dateString + "'.", e);
        }
      }
    }
  }

  static java.util.Date getDate(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof java.util.Date) {
      final java.util.Date date = (java.util.Date)value;
      return date;
    } else {
      final String string = DataTypes.toString(value);
      return getDate(string);
    }
  }

  static Date getDate(final String dateString) {
    if (Property.hasValue(dateString)) {
      final Pattern pattern = Pattern.compile(DATE_TIME_NANOS_PATTERN);
      final Matcher matcher = pattern.matcher(dateString);
      if (matcher.find()) {
        final int year = getInteger(matcher, 1, 0);
        final int month = getInteger(matcher, 2, 0) - 1;
        final int day = getInteger(matcher, 3, 0);
        final int hour = getInteger(matcher, 4, 0);
        final int minute = getInteger(matcher, 5, 0);
        final int second = getInteger(matcher, 6, 0);
        int millisecond = getInteger(matcher, 7, 0);
        final Calendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
        if (millisecond != 0) {
          BigDecimal number = new BigDecimal("0." + millisecond);
          number = number.multiply(BigDecimal.valueOf(1000)).setScale(0, RoundingMode.HALF_DOWN);
          millisecond = number.intValue();
          calendar.set(Calendar.MILLISECOND, millisecond);
        }
        return calendar.getTime();
      }
      throw new IllegalArgumentException("Value '" + dateString
        + "' is not a valid date-time, expecting 'yyyy-MM-dd HH:mm:ss.SSS'.");
    } else {
      return null;
    }
  }

  static Date getDate(final String pattern, final String dateString) {
    final DateFormat format = new SimpleDateFormat(pattern);
    return getDate(format, dateString);
  }

  static int getInteger(final Matcher matcher, final int groupIndex, final int defaultValue) {
    final String group = matcher.group(groupIndex);
    if (Property.hasValue(group)) {
      return Integer.parseInt(group);
    } else {
      return defaultValue;
    }
  }

  static Calendar getIsoCalendar(String dateString) {
    if (Property.hasValue(dateString)) {
      dateString = dateString.trim();
      final int length = dateString.length();

      if (length < 4) {
        throw new IllegalArgumentException(dateString + " is not a valid ISO 8601 date");
      } else {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");

        final int year = Integer.valueOf(dateString.substring(0, 4));
        int month = 0;
        int day = 1;
        int hour = 1;
        int minute = 1;
        int second = 0;
        int millis = 0;
        if (length >= 7) {
          month = Integer.valueOf(dateString.substring(5, 7)) - 1;
          if (length >= 10) {
            day = Integer.valueOf(dateString.substring(8, 10));
            if (length >= 13) {
              hour = Integer.valueOf(dateString.substring(11, 13));
              if (length >= 16) {
                minute = Integer.valueOf(dateString.substring(14, 16));
                if (length >= 19) {
                  second = Integer.valueOf(dateString.substring(17, 19));
                }
              }

              if (length > 19) {
                int tzIndex = 19;
                if (dateString.charAt(tzIndex) == '.') {
                  final int millisIndex = 20;
                  tzIndex = 20;
                  while (tzIndex < length && Character.isDigit(dateString.charAt(tzIndex))) {
                    tzIndex++;
                  }
                  if (millisIndex != tzIndex) {
                    final String millisString = dateString.substring(millisIndex, tzIndex);
                    millis = Integer.valueOf(millisString);
                    if (millisString.length() == 1) {
                      millis = millis * 100;
                    } else if (millisString.length() == 2) {
                      millis = millis * 10;
                    }
                  }

                }
                if (tzIndex < length) {
                  final char tzChar = dateString.charAt(tzIndex);
                  if (tzChar == 'Z') {
                  } else if (tzChar == '+' || tzChar == '-') {
                    if (tzIndex + 5 < length) {
                      final String tzString = dateString.substring(tzIndex, tzIndex + 6);
                      timeZone = TimeZone.getTimeZone("GMT" + tzString);
                    }
                  }
                }
              }

            }
          }

        }
        final Calendar calendar = new GregorianCalendar(timeZone);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millis);

        return calendar;
      }
    } else {
      return null;
    }
  }

  static Date getIsoDate(final String dateString) {
    final Calendar calendar = getIsoCalendar(dateString);
    return calendar.getTime();
  }

  static java.sql.Date getIsoSqlDate(final String dateString) {
    final Calendar calendar = getIsoCalendar(dateString);
    final long time = calendar.getTimeInMillis();
    return new java.sql.Date(time);
  }

  static Timestamp getIsoTimestamp(final String dateString) {
    final Calendar calendar = getIsoCalendar(dateString);
    final long time = calendar.getTimeInMillis();
    return new Timestamp(time);
  }

  static java.sql.Date getSqlDate() {
    return new java.sql.Date(System.currentTimeMillis());
  }

  static java.sql.Date getSqlDate(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof java.sql.Date) {
      final java.sql.Date date = (java.sql.Date)value;
      return date;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return new java.sql.Date(date.getTime());
    } else {
      return getSqlDate(value.toString());
    }
  }

  static java.sql.Date getSqlDate(final String dateString) {
    if (Property.hasValue(dateString)) {
      final Pattern pattern = Pattern.compile(DATE_TIME_NANOS_PATTERN);
      final Matcher matcher = pattern.matcher(dateString);
      if (matcher.find()) {
        final int year = getInteger(matcher, 1, 0);
        final int month = getInteger(matcher, 2, 0) - 1;
        final int day = getInteger(matcher, 3, 0);
        int millisecond = getInteger(matcher, 7, 0);
        final Calendar calendar = new GregorianCalendar(year, month, day);
        if (millisecond != 0) {
          BigDecimal number = new BigDecimal("0." + millisecond);
          number = number.multiply(BigDecimal.valueOf(1000)).setScale(0, RoundingMode.HALF_DOWN);
          millisecond = number.intValue();
          calendar.set(Calendar.MILLISECOND, millisecond);
        }
        final long timeInMillis = calendar.getTimeInMillis();
        return new java.sql.Date(timeInMillis);
      }
      throw new IllegalArgumentException("Value '" + dateString
        + "' is not a valid date-time, expecting 'yyyy-MM-dd HH:mm:ss.SSS'.");
    } else {
      return null;
    }
  }

  static java.sql.Date getSqlDate(final String pattern, final String dateString) {
    final Date date = getDate(pattern, dateString);
    if (date == null) {
      return null;
    } else {
      final long time = date.getTime();
      return new java.sql.Date(time);
    }
  }

  static Timestamp getTimestamp() {
    return new Timestamp(System.currentTimeMillis());
  }

  static Timestamp getTimestamp(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Timestamp) {
      final Timestamp date = (Timestamp)value;
      return date;
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return new Timestamp(date.getTime());
    } else {
      return getTimestamp(value.toString());
    }
  }

  static Timestamp getTimestamp(final String dateString) {
    if (Property.hasValue(dateString)) {
      final Pattern pattern = Pattern.compile(DATE_TIME_NANOS_PATTERN);
      final Matcher matcher = pattern.matcher(dateString);
      if (matcher.find()) {
        final int year = getInteger(matcher, 1, 0);
        final int month = getInteger(matcher, 2, 0) - 1;
        final int day = getInteger(matcher, 3, 0);
        final int hour = getInteger(matcher, 4, 0);
        final int minute = getInteger(matcher, 5, 0);
        final int second = getInteger(matcher, 6, 0);
        int nanoSecond = getInteger(matcher, 7, 0);
        final Calendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
        final long timeInMillis = calendar.getTimeInMillis();
        final Timestamp time = new Timestamp(timeInMillis);
        if (nanoSecond != 0) {
          BigDecimal number = new BigDecimal("0." + nanoSecond);
          number = number.multiply(BigDecimal.valueOf(1000000000)).setScale(0,
            RoundingMode.HALF_DOWN);
          nanoSecond = number.intValue();
          time.setNanos(nanoSecond);
        }
        return time;
      }
      throw new IllegalArgumentException("Value '" + dateString
        + "' is not a valid timestamp, expecting 'yyyy-MM-dd HH:mm:ss.SSS'.");
    } else {
      return null;
    }
  }

  static Timestamp getTimestamp(final String pattern, final String dateString) {

    final Date date = getDate(pattern, dateString);
    if (date == null) {
      return null;
    } else {
      final long time = date.getTime();
      return new Timestamp(time);
    }
  }

  static int getYear() {
    final Calendar calendar = Calendar.getInstance();
    return calendar.get(Calendar.YEAR);
  }

  static String toDateTimeString(final Date date) {
    return format("yyyy-MM-dd HH:mm:ss.SSS", date);
  }

  static String toDateTimeString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Date date = getDate(value);
      return toDateTimeString(date);
    }
  }

  static String toSqlDateString(final Date date) {
    if (date == null) {
      return null;
    } else {
      return format("yyyy-MM-dd", date);
    }
  }

  static String toSqlDateString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final java.sql.Date date = getSqlDate(value);
      return toSqlDateString(date);
    }
  }

  static String toTimestampString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Timestamp timestamp = getTimestamp(value);
      return toTimestampString(timestamp);
    }
  }

  static String toTimestampString(final Timestamp date) {
    if (date == null) {
      return null;
    } else {
      return date.toString();
    }
  }
}
