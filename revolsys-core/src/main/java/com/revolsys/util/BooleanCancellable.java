package com.revolsys.util;

public class BooleanCancellable implements Cancellable {

  private boolean cancelled = false;

  public void cancel() {
    this.cancelled = true;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public String toString() {
    return Boolean.toString(this.cancelled);
  }
}
