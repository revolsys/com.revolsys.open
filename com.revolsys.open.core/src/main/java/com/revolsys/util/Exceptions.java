package com.revolsys.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

public interface Exceptions {
  static void log(final Class<?> clazz, final String message, final Throwable e) {
    LoggerFactory.getLogger(clazz).error(message, e);
  }

  static void log(final Class<?> clazz, final Throwable e) {
    log(clazz, e.getMessage(), e);
  }

  static void log(final String name, final String message, final Throwable e) {
    LoggerFactory.getLogger(name).error(message, e);
  }

  static void log(final String name, final Throwable e) {
    log(name, e.getMessage(), e);
  }

  @SuppressWarnings("unchecked")
  static <T> T throwCauseException(final Throwable e) {
    final Throwable cause = e.getCause();
    return (T)throwUncheckedException(cause);
  }

  @SuppressWarnings("unchecked")
  static <T> T throwUncheckedException(final Throwable e) {
    if (e instanceof InvocationTargetException) {
      return (T)throwCauseException(e);
    } else if (e instanceof RuntimeException) {
      throw (RuntimeException)e;
    } else if (e instanceof Error) {
      throw (Error)e;
    } else {
      throw wrap(e);
    }
  }

  static String toString(final Throwable e) {
    final StringWriter string = new StringWriter();
    final PrintWriter out = new PrintWriter(string);
    e.printStackTrace(out);
    return string.toString();
  }

  static WrappedException wrap(final Throwable e) {
    return new WrappedException(e);
  }
}
