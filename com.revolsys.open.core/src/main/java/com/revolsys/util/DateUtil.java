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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {

  private static final String DATE_TIME_NANOS_PATTERN = "\\s*(\\d{4})-(\\d{2})-(\\d{2})(?:[\\sT]+(\\d{2})\\:(\\d{2})\\:(\\d{2})(?:\\.(\\d{1,9}))?)?\\s*";

  public static String format(final DateFormat format, final Date date) {
    return format.format(date);
  }

  public static String format(final int dateStyle, final int timeStyle,
    final Timestamp timestamp) {
    final DateFormat format = DateFormat.getDateTimeInstance(dateStyle,
      timeStyle);
    return format(format, timestamp);
  }

  public static String format(final String pattern) {
    return format(pattern, new Date(System.currentTimeMillis()));
  }

  public static String format(final String pattern, final Calendar calendar) {
    if (calendar == null) {
      return null;
    } else {
      final Date date = calendar.getTime();
      return format(pattern, date);
    }
  }

  public static String format(final String pattern, final Date date) {
    if (date == null) {
      return null;
    } else {
      final DateFormat format = new SimpleDateFormat(pattern);
      return format(format, date);
    }
  }

  public static Calendar getCalendar(final String dateString) {
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
        final Calendar calendar = new GregorianCalendar(year, month, day, hour,
          minute, second);
        if (millisecond != 0) {
          BigDecimal number = new BigDecimal("0." + millisecond);
          number = number.multiply(BigDecimal.valueOf(100)).setScale(0,
            RoundingMode.HALF_DOWN);
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

  public static Date getDate() {
    return new Date(System.currentTimeMillis());
  }

  public static Date getDate(final DateFormat format, final String dateString) {
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
          throw new IllegalArgumentException("Invalid date  '" + dateString
            + "'.", e);
        }
      }
    }
  }

  public static Date getDate(final String dateString) {
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
        final Calendar calendar = new GregorianCalendar(year, month, day, hour,
          minute, second);
        if (millisecond != 0) {
          BigDecimal number = new BigDecimal("0." + millisecond);
          number = number.multiply(BigDecimal.valueOf(1000)).setScale(0,
            RoundingMode.HALF_DOWN);
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

  public static Date getDate(final String pattern, final String dateString) {
    final DateFormat format = new SimpleDateFormat(pattern);
    return getDate(format, dateString);
  }

  public static int getInteger(final Matcher matcher, final int groupIndex,
    final int defaultValue) {
    final String group = matcher.group(groupIndex);
    if (Property.hasValue(group)) {
      return Integer.parseInt(group);
    } else {
      return defaultValue;
    }
  }

  public static java.sql.Date getSqlDate() {
    return new java.sql.Date(System.currentTimeMillis());
  }

  public static java.sql.Date getSqlDate(final String dateString) {
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
          number = number.multiply(BigDecimal.valueOf(1000)).setScale(0,
            RoundingMode.HALF_DOWN);
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

  public static java.sql.Date getSqlDate(final String pattern,
    final String dateString) {
    final Date date = getDate(pattern, dateString);
    if (date == null) {
      return null;
    } else {
      final long time = date.getTime();
      return new java.sql.Date(time);
    }
  }

  public static Timestamp getTimestamp() {
    return new Timestamp(System.currentTimeMillis());
  }

  public static Timestamp getTimestamp(final String dateString) {
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
        final Calendar calendar = new GregorianCalendar(year, month, day, hour,
          minute, second);
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

  public static Timestamp getTimestamp(final String pattern,
    final String dateString) {

    final Date date = getDate(pattern, dateString);
    if (date == null) {
      return null;
    } else {
      final long time = date.getTime();
      return new Timestamp(time);
    }
  }

  public static int getYear() {
    final Calendar calendar = Calendar.getInstance();
    return calendar.get(Calendar.YEAR);
  }
}
