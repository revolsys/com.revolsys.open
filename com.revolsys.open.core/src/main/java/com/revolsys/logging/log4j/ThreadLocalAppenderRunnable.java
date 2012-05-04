package com.revolsys.logging.log4j;

public class ThreadLocalAppenderRunnable implements Runnable {
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
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

}
