package com.revolsys.log;

import java.io.File;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;

public class LogAppender {

  static void addAppender(final Class<?> loggerName, final Appender appender) {
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

  static void addAppender(final String loggerName, final Appender appender) {
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

  public static void addFileAppender(final String category, final File logFile,
    final String pattern, final boolean append) {
    final FileAppender appender = newFileAppender(logFile, pattern, append);
    addAppender(category, appender);
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

  public static void addRootFileAppender(final File logFile, final String pattern,
    final boolean append) {
    final FileAppender appender = newFileAppender(logFile, pattern, append);
    addRootAppender(appender);
  }

  public static Object addRootFileAppender(final File logFile, final String pattern,
    final boolean append, final String category, final String... messages) {
    final FileAppender appender = newFileAppender(logFile, pattern, append);
    for (final String message : messages) {
      final LogEvent event = Log4jLogEvent.newBuilder()//
        .setLoggerName(category) //
        .setLevel(org.apache.logging.log4j.Level.INFO) //
        .setMessage(new SimpleMessage(message.toString())) //
        .build();

      appender.append(event);
    }
    addRootAppender(appender);
    return appender;
  }

  static org.apache.logging.log4j.core.Logger getLog4jCoreLogger(final Class<?> loggerName) {
    org.apache.logging.log4j.core.Logger logger;
    if (loggerName == null) {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
    } else {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger(loggerName);
    }
    return logger;
  }

  static org.apache.logging.log4j.core.Logger getLog4jCoreLogger(final String name) {
    org.apache.logging.log4j.core.Logger logger;
    if (name == null) {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
    } else {
      logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger(name);
    }
    return logger;
  }

  private static FileAppender newFileAppender(final File file, final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    return FileAppender.newBuilder() //
      .withLayout(layout)//
      .withName("file")
      .withFileName(file.getAbsolutePath())
      .build();
  }

  static FileAppender newFileAppender(final File file, final String pattern, final boolean append) {
    final PatternLayout layout = newLayout(pattern);
    return FileAppender.newBuilder() //
      .withLayout(layout)//
      .withName("file")
      .withFileName(file.getAbsolutePath())
      .withAppend(append)
      .build();
  }

  private static FileAppender newFileAppender(final Path file, final String pattern) {
    final PatternLayout layout = newLayout(pattern);
    return FileAppender.newBuilder() //
      .withLayout(layout)//
      .withName("file")
      .withFileName(file.toString())
      .build();
  }

  private static PatternLayout newLayout(final String pattern) {
    return PatternLayout.newBuilder() //
      .withPattern(pattern)//
      .build();
  }

  public static void removeAllAppenders() {
    final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager
      .getRootLogger();
    removeAllAppenders(logger);
  }

  private static void removeAllAppenders(final org.apache.logging.log4j.core.Logger logger) {
    for (final Appender appender : logger.getAppenders().values()) {
      logger.removeAppender(appender);
    }
  }

  private static void removeRootAppender(final Appender appender) {
    final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager
      .getRootLogger();
    logger.removeAppender(appender);
  }

  public static void removeRootAppender(final Object appender) {
    if (appender instanceof Appender) {
      final Appender a = (Appender)appender;
      a.stop();
      removeRootAppender(a);

    }
  }

  public static void setLevel(final String name, final org.slf4j.event.Level level) {
    setLevel(name, level.toString());
  }

  public static void setLevel(final String name, final String level) {
    final org.apache.logging.log4j.core.Logger logger = getLog4jCoreLogger(name);
    final Level level2 = Level.toLevel(level.toUpperCase());
    logger.setLevel(level2);
  }

}
