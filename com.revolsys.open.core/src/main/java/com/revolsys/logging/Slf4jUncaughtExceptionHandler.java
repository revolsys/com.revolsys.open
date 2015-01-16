package com.revolsys.logging;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.LoggerFactory;

public class Slf4jUncaughtExceptionHandler implements UncaughtExceptionHandler {
  public static void init() {
  }

  static {
    Thread.setDefaultUncaughtExceptionHandler(new Slf4jUncaughtExceptionHandler());
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
  }
}
