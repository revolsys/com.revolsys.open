package com.revolsys.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

public final class ExceptionUtil {
  public static void log(final Class<?> clazz, final String message, final Throwable exception) {
    LoggerFactory.getLogger(clazz).error(message, exception);
  }

  public static void log(final Class<?> clazz, final Throwable exception) {
    log(clazz, exception.getMessage(), exception);
  }

  @SuppressWarnings("unchecked")
  public static <T> T throwCauseException(final Throwable e) {
    final Throwable cause = e.getCause();
    return (T)throwUncheckedException(cause);
  }

  @SuppressWarnings("unchecked")
  public static <T> T throwUncheckedException(final Throwable exception) {
    if (exception instanceof InvocationTargetException) {
      return (T)throwCauseException(exception);
    } else if (exception instanceof RuntimeException) {
      throw (RuntimeException)exception;
    } else if (exception instanceof Error) {
      throw (Error)exception;
    } else {
      throw new WrappedException(exception);
    }
  }

  public static String toString(final Throwable e) {
    final StringWriter string = new StringWriter();
    final PrintWriter out = new PrintWriter(string);
    e.printStackTrace(out);
    return string.toString();
  }
}
