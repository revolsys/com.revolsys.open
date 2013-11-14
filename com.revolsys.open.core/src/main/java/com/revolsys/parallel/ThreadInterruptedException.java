package com.revolsys.parallel;

public class ThreadInterruptedException extends RuntimeException {
  public ThreadInterruptedException() {
    super("Thread was interrupted");
  }

  public ThreadInterruptedException(final InterruptedException e) {
    super("Thread was interrupted", e);
    Thread.currentThread().interrupt();
  }
}
