package com.revolsys.parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(LoggingRunnable.class);

  Runnable runnable;

  public LoggingRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  public void run() {
    try {
      runnable.run();
    } catch (RuntimeException e) {
      LOG.error(e.getMessage(), e);
      throw e;
    } catch (Error e) {
      LOG.error(e.getMessage(), e);
      throw e;
    }

  }
}
