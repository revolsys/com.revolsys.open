package com.revolsys.logging.log4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalAppenderRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalAppenderRunnable.class);

  private final ThreadLocalFileAppender appender;

  private String logFile;

  private final Runnable process;

  public ThreadLocalAppenderRunnable(final Runnable process) {
    this.process = process;
    this.appender = ThreadLocalFileAppender.getAppender();
    if (appender != null) {
      this.logFile = appender.getLocalFile();
    }
  }

  public void run() {
    try {
      if (appender != null) {
        try {
          appender.setLocalFile(logFile);
        } catch (final Throwable t) {
          t.printStackTrace();
        }
      }
      process.run();
    } catch (Throwable t) {
      LOG.error("Error running " + process, t);
    }
  }

}
