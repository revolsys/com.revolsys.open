package com.revolsys.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Exceptions {
  static void debug(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    debug(name, message, e);
  }

  static void debug(final String name, final String message, Throwable e) {
    while (e instanceof WrappedException) {
      final WrappedException wrappedException = (WrappedException)e;
      e = wrappedException.getCause();
    }
    final Logger logger = LoggerFactory.getLogger(name);
    logger.debug(message, e);
  }

  static void error(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    error(name, message, e);
  }

  static void error(final Class<?> clazz, final Throwable e) {
    final String message = e.getMessage();
    error(clazz, message, e);
  }

  static void error(final Object object, final String message, final Throwable e) {
    final Class<?> clazz = object.getClass();
    error(clazz, message, e);
  }

  static void error(final Object object, final Throwable e) {
    final Class<?> clazz = object.getClass();
    error(clazz, e);
  }

  static void error(final String name, final String message, Throwable e) {
    while (e instanceof WrappedException) {
      final WrappedException wrappedException = (WrappedException)e;
      e = wrappedException.getCause();
    }
    final Logger logger = LoggerFactory.getLogger(name);
    logger.error(message, e);
  }

  static void error(final String name, final Throwable e) {
    final String message = e.getMessage();
    error(name, message, e);
  }

  static void info(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    info(name, message, e);
  }

  static void info(final Class<?> clazz, final Throwable e) {
    final String message = e.getMessage();
    info(clazz, message, e);
  }

  static void info(final String name, final String message, Throwable e) {
    while (e instanceof WrappedException) {
      final WrappedException wrappedException = (WrappedException)e;
      e = wrappedException.getCause();
    }
    final Logger logger = LoggerFactory.getLogger(name);
    logger.info(message, e);
  }

  @SuppressWarnings("unchecked")
  static <T> T throwCauseException(final Throwable e) {
    final Throwable cause = e.getCause();
    return (T)throwUncheckedException(cause);
  }

  @SuppressWarnings("unchecked")
  static <T> T throwUncheckedException(final Throwable e) {
    if (e == null) {
      return null;
    } else if (e instanceof InvocationTargetException) {
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

  static WrappedException wrap(final String message, final Throwable e) {
    return new WrappedException(message, e);
  }

  static WrappedException wrap(final Throwable e) {
    return new WrappedException(e);
  }
}
