package com.revolsys.parallel;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
  private int threadNumber;

  private final String name;

  public NamedThreadFactory(final String name) {
    this.name = name;
  }

  public Thread newThread(final Runnable r) {
    return new Thread(new LoggingRunnable(r), name + "-" + threadNumber++);
  }
}
