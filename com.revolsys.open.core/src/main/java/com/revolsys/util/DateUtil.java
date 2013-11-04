package com.revolsys.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.StringUtils;

public class DateUtil {

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

  public static Date parse(final DateFormat format, final String dateString) {
    if (!StringUtils.hasText(dateString)) {
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

  public static Date parse(final String pattern, final String dateString) {
    final DateFormat format = new SimpleDateFormat(pattern);
    return parse(format, dateString);
  }
}
