package com.revolsys.logging;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.set.Sets;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
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

  public static void setLevel(final String name, final String level) {
    org.apache.log4j.Logger logger;
    if (name == null) {
      logger = org.apache.log4j.Logger.getRootLogger();
    } else {
      logger = org.apache.log4j.Logger.getLogger(name);
    }
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
