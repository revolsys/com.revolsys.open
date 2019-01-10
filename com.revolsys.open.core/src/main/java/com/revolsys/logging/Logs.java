package com.revolsys.logging;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.set.Sets;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.WrappedException;

public class Logs {

  public static void addAppender(final Class<?> loggerName, final Appender appender) {
    if (!appender.isStarted()) {
      appender.start();
    }
    final org.apache.logging.log4j.core.Logger logger = getLog4jCoreLogger(loggerName);
    logger.addAppender(appender);
  }

  public static void addAppender(final Class<?> loggerName, final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    final Appender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
    addAppender(loggerName, appender);
  }

  public static void addAppender(final String loggerName, final Appender appender) {
    if (!appender.isStarted()) {
      appender.start();
    }
    final org.apache.logging.log4j.core.Logger logger = getLog4jCoreLogger(loggerName);
    logger.addAppender(appender);
  }

  public static void addAppender(final String loggerName, final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    final Appender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
    addAppender(loggerName, appender);
  }

  public static void addRootAppender(final Appender appender) {
    final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager
      .getRootLogger();
    if (!appender.isStarted()) {
      appender.start();
    }
    logger.addAppender(appender);
  }

  public static void addRootAppender(final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    final Appender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
    addRootAppender(appender);
  }

  public static FileAppender addRootFileAppender(final File logFile, final String pattern,
    final boolean append) {
    final FileAppender appender = newFileAppender(logFile, pattern, append);
    addRootAppender(appender);
    return appender;
  }

  public static void debug(final Class<?> clazz, final String message) {
    final String name = clazz.getName();
    debug(name, message);
  }

  public static void debug(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    debug(name, message, e);
  }

  public static void debug(final Class<?> clazz, final Throwable e) {
    final String name = clazz.getName();
    debug(name, e);
  }

  public static void debug(final Object object, final String message) {
    final Class<?> clazz = object.getClass();
    debug(clazz, message);
  }

  public static void debug(final Object object, final String message, final Throwable e) {
    final Class<?> clazz = object.getClass();
    debug(clazz, message, e);
  }

  public static void debug(final Object object, final Throwable e) {
    final Class<?> clazz = object.getClass();
    debug(clazz, e);
  }

  public static void debug(final String name, final String message) {
    final Logger logger = LoggerFactory.getLogger(name);
    logger.debug(message);
  }

  public static void debug(final String name, final String message, final Throwable e) {
    final StringBuilder messageText = new StringBuilder();
    final Throwable logException = getMessageAndException(messageText, message, e);

    final Logger logger = LoggerFactory.getLogger(name);
    logger.debug(messageText.toString(), logException);
  }

  public static void debug(final String name, final Throwable e) {
    final String message = e.getMessage();
    debug(name, message, e);
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

  public static void error(final String name, final String message, final Throwable e) {
    final StringBuilder messageText = new StringBuilder();
    final Throwable logException = getMessageAndException(messageText, message, e);

    final Logger logger = LoggerFactory.getLogger(name);
    logger.error(messageText.toString(), logException);
  }

  public static void error(final String name, final Throwable e) {
    final String message = e.getMessage();
    error(name, message, e);
  }

  public static org.apache.logging.log4j.core.Logger getLog4jCoreLogger(final Class<?> loggerName) {
    org.apache.logging.log4j.core.Logger logger;
    if (loggerName == null) {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
    } else {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger(loggerName);
    }
    return logger;
  }

  public static org.apache.logging.log4j.core.Logger getLog4jCoreLogger(final String name) {
    org.apache.logging.log4j.core.Logger logger;
    if (name == null) {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
    } else {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger(name);
    }
    return logger;
  }

  public static Throwable getMessageAndException(final StringBuilder messageText,
    final String message, final Throwable e) {
    Throwable logException = e;
    final Set<String> messages = Sets.newLinkedHash(message);
    if (Property.hasValue(message)) {
      messageText.append(message);
    }
    while (logException instanceof WrappedException
      || logException instanceof NestedRuntimeException) {
      if (messageText.length() > 0) {
        messageText.append('\n');
      }
      messageText.append(logException.getClass().getName());
      messageText.append(": ");
      final String wrappedMessage = logException.getMessage();
      if (messages.add(wrappedMessage)) {
        messageText.append(wrappedMessage);
      }
      final Throwable cause = logException.getCause();
      if (cause == null) {
        break;
      } else {
        logException = cause;
      }
    }
    if (logException instanceof SQLException) {
      final SQLException sqlException = (SQLException)logException;
      final List<Throwable> exceptions = Lists.toArray(sqlException);
      final int exceptionCount = exceptions.size();
      if (exceptionCount > 0) {
        logException = exceptions.remove(exceptionCount - 1);
        for (final Throwable throwable : exceptions) {
          if (messageText.length() > 0) {
            messageText.append('\n');
          }
          if (throwable == sqlException) {
            messageText.append(sqlException.getClass().getName());
            messageText.append(": ");
            final String wrappedMessage = sqlException.getMessage();
            if (messages.add(wrappedMessage)) {
              messageText.append(wrappedMessage);
            }
          } else {
            messageText.append(Exceptions.toString(throwable));
          }
        }
      }
    }
    return logException;
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

  public static void info(final String name, final String message, final Throwable e) {
    final StringBuilder messageText = new StringBuilder();
    final Throwable logException = getMessageAndException(messageText, message, e);

    final Logger logger = LoggerFactory.getLogger(name);
    logger.info(messageText.toString(), logException);
  }

  public static boolean isDebugEnabled(final Class<?> logCateogory) {
    final Logger logger = LoggerFactory.getLogger(logCateogory);
    return logger.isDebugEnabled();
  }

  public static boolean isDebugEnabled(final Object logCateogory) {
    final Class<?> logClass = logCateogory.getClass();
    return isDebugEnabled(logClass);
  }

  public static FileAppender newFileAppender(final File file, final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    return FileAppender.newBuilder() //
      .withLayout(layout)//
      .withName("file")
      .withFileName(file.getAbsolutePath())
      .build();
  }

  public static FileAppender newFileAppender(final File file, final String pattern,
    final boolean append) {
    final PatternLayout layout = newLayout(pattern);
    return FileAppender.newBuilder() //
      .withLayout(layout)//
      .withName("file")
      .withFileName(file.getAbsolutePath())
      .withAppend(append)
      .build();
  }

  public static FileAppender newFileAppender(final Path file, final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    return FileAppender.newBuilder() //
      .withLayout(layout)//
      .withName("file")
      .withFileName(file.toString())
      .build();
  }

  public static PatternLayout newLayout(final String pattern) {
    return PatternLayout.newBuilder() //
      .withPattern(pattern)//
      .build();
  }

  public static void removeAllAppenders() {
    final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager
      .getRootLogger();
    removeAllAppenders(logger);
  }

  public static void removeAllAppenders(final org.apache.logging.log4j.core.Logger logger) {
    for (final Appender appender : logger.getAppenders().values()) {
      logger.removeAppender(appender);
    }
  }

  public static void removeRootAppender(final Appender appender) {
    final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager
      .getRootLogger();
    logger.removeAppender(appender);
  }

  public static void setLevel(final String name, final org.slf4j.event.Level level) {
    setLevel(name, level.toString());
  }

  public static void setLevel(final String name, final String level) {
    final org.apache.logging.log4j.core.Logger logger = getLog4jCoreLogger(name);
    final Level level2 = Level.toLevel(level.toUpperCase());
    logger.setLevel(level2);
  }

  public static void warn(final Class<?> clazz, final String message) {
    final String name = clazz.getName();
    warn(name, message);
  }

  public static void warn(final Class<?> clazz, final String message, final Throwable e) {
    final String name = clazz.getName();
    warn(name, message, e);
  }

  public static void warn(final Class<?> clazz, final Throwable e) {
    final String name = clazz.getName();
    warn(name, e);
  }

  public static void warn(final Object object, final String message) {
    final Class<?> clazz = object.getClass();
    warn(clazz, message);
  }

  public static void warn(final Object object, final String message, final Throwable e) {
    final Class<?> clazz = object.getClass();
    warn(clazz, message, e);
  }

  public static void warn(final Object object, final Throwable e) {
    final Class<?> clazz = object.getClass();
    warn(clazz, e);
  }

  public static void warn(final String name, final String message) {
    final Logger logger = LoggerFactory.getLogger(name);
    logger.warn(message);
  }

  public static void warn(final String name, final String message, final Throwable e) {
    final StringBuilder messageText = new StringBuilder();
    final Throwable logException = getMessageAndException(messageText, message, e);

    final Logger logger = LoggerFactory.getLogger(name);
    logger.warn(messageText.toString(), logException);
  }

  public static void warn(final String name, final Throwable e) {
    final String message = e.getMessage();
    warn(name, message, e);
  }

}
