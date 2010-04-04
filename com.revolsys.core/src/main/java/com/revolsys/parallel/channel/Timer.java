package com.revolsys.parallel.channel;

public class Timer implements AltingChannelInput {
  private long time;

  public Timer(long time) {
    this.time = time;
  }

  public boolean disable() {
    return isTimeout();
  }

  public boolean enable(final MultiChannelReadSelector alt) {
    return isTimeout();
  }

  public boolean isClosed() {
    return false;
  }

  public boolean isTimeout() {
    boolean timeout = System.currentTimeMillis() > time;
    return timeout;
  }

  public long getWaitTime() {
    long waitTime = time - System.currentTimeMillis();
    return waitTime;
  }
}
