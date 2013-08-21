package com.revolsys.logging;

import org.slf4j.LoggerFactory;

public class LoggingRunnable implements Runnable {
  private final Runnable runnable;

  public LoggingRunnable(final Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void run() {
    try {
      runnable.run();
    } catch (final Throwable e) {
      Class<? extends Runnable> logClass;
      if (runnable == null) {
        logClass = getClass();
      } else {
        logClass = runnable.getClass();
      }
      LoggerFactory.getLogger(logClass).error(e.getMessage(), e);
    }

  }
}
