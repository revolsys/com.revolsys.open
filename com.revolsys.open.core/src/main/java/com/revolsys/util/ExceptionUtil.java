package com.revolsys.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

public final class ExceptionUtil {
  public static void log(final Class<?> clazz, final String message, final Throwable e) {
    LoggerFactory.getLogger(clazz).error(message, e);
  }

  public static void log(final Class<?> clazz, final Throwable e) {
    log(clazz, e.getMessage(), e);
  }

  @SuppressWarnings("unchecked")
  public static <T> T throwCauseException(final Throwable e) {
    final Throwable cause = e.getCause();
    return (T)throwUncheckedException(cause);
  }

  @SuppressWarnings("unchecked")
  public static <T> T throwUncheckedException(final Throwable e) {
    if (e instanceof InvocationTargetException) {
      return (T)throwCauseException(e);
    } else if (e instanceof RuntimeException) {
      throw (RuntimeException)e;
    } else if (e instanceof Error) {
      throw (Error)e;
    } else {
      throw new WrappedException(e);
    }
  }

  public static String toString(final Throwable e) {
    final StringWriter string = new StringWriter();
    final PrintWriter out = new PrintWriter(string);
    e.printStackTrace(out);
    return string.toString();
  }
}
