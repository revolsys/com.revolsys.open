package com.revolsys.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.util.WrappedException;

public class Logs {

  public static void debug(final Class<?> clazz, final String message) {
    final String name = clazz.getName();
    debug(name, message);
  }

  public static void debug(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    debug(name, message, e);
  }

  public static void debug(final Object object, final String message) {
    final Class<?> clazz = object.getClass();
    debug(clazz, message);
  }

  public static void debug(final String name, final String message) {
    final Logger logger = LoggerFactory.getLogger(name);
    logger.debug(message);
  }

  public static void debug(final String name, final String message, Throwable e) {
    while (e instanceof WrappedException) {
      final WrappedException wrappedException = (WrappedException)e;
      e = wrappedException.getCause();
    }
    final Logger logger = LoggerFactory.getLogger(name);
    logger.debug(message, e);
  }

  public static void error(final Class<?> clazz, final String message) {
    final String name = clazz.getName();
    error(name, message);
  }

  public static void error(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    error(name, message, e);
  }

  public static void error(final Class<?> clazz, final Throwable e) {
    final String message = e.getMessage();
    error(clazz, message, e);
  }

  public static void error(final Object object, final String message) {
    final Class<?> clazz = object.getClass();
    error(clazz, message);
  }

  public static void error(final Object object, final String message, final Throwable e) {
    final Class<?> clazz = object.getClass();
    error(clazz, message, e);
  }

  public static void error(final Object object, final Throwable e) {
    final Class<?> clazz = object.getClass();
    error(clazz, e);
  }

  public static void error(final String name, final String message) {
    final Logger logger = LoggerFactory.getLogger(name);
    logger.error(message);
  }

  public static void error(final String name, final String message, Throwable e) {
    while (e instanceof WrappedException) {
      final WrappedException wrappedException = (WrappedException)e;
      e = wrappedException.getCause();
    }
    final Logger logger = LoggerFactory.getLogger(name);
    logger.error(message, e);
  }

  public static void error(final String name, final Throwable e) {
    final String message = e.getMessage();
    error(name, message, e);
  }

  public static void info(final Class<?> clazz, final String message) {
    final String name = clazz.getName();
    info(name, message);
  }

  public static void info(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    info(name, message, e);
  }

  public static void info(final Class<?> clazz, final Throwable e) {
    final String message = e.getMessage();
    info(clazz, message, e);
  }

  public static void info(final Object object, final String message) {
    final Class<?> clazz = object.getClass();
    info(clazz, message);
  }

  public static void info(final String name, final String message) {
    final Logger logger = LoggerFactory.getLogger(name);
    logger.info(message);
  }

  public static void info(final String name, final String message, Throwable e) {
    while (e instanceof WrappedException) {
      final WrappedException wrappedException = (WrappedException)e;
      e = wrappedException.getCause();
    }
    final Logger logger = LoggerFactory.getLogger(name);
    logger.info(message, e);
  }

}
