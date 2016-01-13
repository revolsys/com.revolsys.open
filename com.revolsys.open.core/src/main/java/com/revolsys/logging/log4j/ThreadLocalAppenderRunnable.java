package com.revolsys.logging.log4j;

import com.revolsys.parallel.AbstractRunnable;

public class ThreadLocalAppenderRunnable extends AbstractRunnable {
  private final ThreadLocalFileAppender appender;

  private String logFile;

  private final Runnable process;

  public ThreadLocalAppenderRunnable(final Runnable process) {
    this.process = process;
    this.appender = ThreadLocalFileAppender.getAppender();
    if (this.appender != null) {
      this.logFile = this.appender.getLocalFile();
    }
  }

  @Override
  public void runDo() {
    try {
      if (this.appender != null) {
        try {
          this.appender.setLocalFile(this.logFile);
        } catch (final Throwable t) {
          t.printStackTrace();
        }
      }
      this.process.run();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

}
