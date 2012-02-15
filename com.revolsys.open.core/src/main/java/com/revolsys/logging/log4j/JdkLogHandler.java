package com.revolsys.logging.log4j;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class JdkLogHandler extends Handler {

  private static final Map LEVEL_PRIORITY = new HashMap();

  static {
    LEVEL_PRIORITY.put(Level.ALL, org.apache.log4j.Level.DEBUG);
    LEVEL_PRIORITY.put(Level.FINEST, org.apache.log4j.Level.DEBUG);
    LEVEL_PRIORITY.put(Level.FINER, org.apache.log4j.Level.DEBUG);
    LEVEL_PRIORITY.put(Level.FINE, org.apache.log4j.Level.DEBUG);
    LEVEL_PRIORITY.put(Level.INFO, org.apache.log4j.Level.INFO);
    LEVEL_PRIORITY.put(Level.CONFIG, org.apache.log4j.Level.INFO);
    LEVEL_PRIORITY.put(Level.WARNING, org.apache.log4j.Level.WARN);
    LEVEL_PRIORITY.put(Level.SEVERE, org.apache.log4j.Level.ERROR);
  }

  @Override
  public void close() throws SecurityException {
  }

  @Override
  public void flush() {
  }

  @Override
  public void publish(final LogRecord record) {
    final Logger log = Logger.getLogger(record.getLoggerName());
    final Level level = record.getLevel();
    final String message = record.getMessage();
    final Throwable exception = record.getThrown();
    final Priority priority = (Priority)LEVEL_PRIORITY.get(level);
    if (exception != null) {
      log.log(priority, message, exception);
    } else {
      log.log(priority, message);

    }
  }
}
