package com.revolsys.parallel.channel;

public class MultiChannelReadSelector {
  private int enabledChannels = 0;

  private long maxWait;

  public synchronized int select(AltingChannelInput... c) {
    return select(Long.MAX_VALUE, c);
  }

  public synchronized int select(long msecs, AltingChannelInput... c) {
    return select(msecs, 0, c);
  }

  public synchronized int select(long msecs, int nsecs, AltingChannelInput... c) {
    if (!enableChannels(c)) {
      try {
        if (msecs == 0)
          wait(Math.min(msecs, maxWait), nsecs);
      } catch (InterruptedException e) {
      }
    }
    return disableChannels(c);
  }

  public synchronized int select(AltingChannelInput[] c, boolean skip) {
    if (skip) {
      enableChannels(c);
      return disableChannels(c);
    } else {
      return select(c);
    }
  }

  public synchronized int select(AltingChannelInput[] c, boolean[] guard) {
    return select(c, guard, Long.MAX_VALUE);
  }

  public synchronized int select(AltingChannelInput[] c, boolean[] guard,
    long msecs) {
    return select(c, guard, msecs, 0);
  }

  public synchronized int select(AltingChannelInput[] c, boolean[] guard,
    long msecs, int nsecs) {
    if (!enableChannels(c, guard)) {
      try {
        wait(Math.min(msecs, maxWait), nsecs);
      } catch (InterruptedException e) {
      }
    }
    return disableChannels(c, guard);
  }

  public synchronized int select(AltingChannelInput[] c, boolean[] guard,
    boolean skip) {
    if (skip) {
      enableChannels(c, guard);
      return disableChannels(c, guard);
    } else {
      return select(c, guard);
    }
  }

  private boolean enableChannels(AltingChannelInput[] channels) {
    enabledChannels = 0;
    maxWait = Long.MAX_VALUE;
    int closedCount = 0;
    for (int i = 0; i < channels.length; i++) {
      AltingChannelInput channel = channels[i];
      if (!channel.isClosed()) {
        if (channel.enable(this)) {
          enabledChannels++;
          return true;
        } else if (channel instanceof Timer) {
          Timer timer = (Timer)channel;
          maxWait = Math.min(maxWait, timer.getWaitTime());
        }
      } else {
        closedCount++;
      }
    }
    return closedCount == channels.length;
  }

  private boolean enableChannels(AltingChannelInput[] channels, boolean[] guard) {
    enabledChannels = 0;
    maxWait = Long.MAX_VALUE;
    int closedCount = 0;
    int activeChannelCount = 0;
    for (int i = 0; i < channels.length; i++) {
      AltingChannelInput channel = channels[i];
      if (guard[i]) {
        activeChannelCount++;
        if (!channel.isClosed()) {
          if (channel.enable(this)) {
            enabledChannels++;
            return true;
          } else if (channel instanceof Timer) {
            Timer timer = (Timer)channel;
            maxWait = Math.min(maxWait, timer.getWaitTime());
          }
        } else {
          closedCount++;
        }
      }
    }
    return closedCount == activeChannelCount;
  }

  private int disableChannels(AltingChannelInput[] channels) {
    int closedCount = 0;
    int selected = -1;
    for (int i = channels.length - 1; i >= 0; i--) {
      AltingChannelInput channel = channels[i];
      if (channel.disable()) {
        selected = i;
      } else if (channel.isClosed()) {
        closedCount++;
      }
    }
    if (closedCount == channels.length) {
      throw new ClosedException();
    } else {
      return selected;
    }

  }

  private int disableChannels(AltingChannelInput[] channels, boolean[] guard) {
    int closedCount = 0;
    int selected = -1;
    for (int i = channels.length - 1; i >= 0; i--) {
      AltingChannelInput channel = channels[i];
      if (guard[i] && channel.disable()) {
        selected = i;
      } else if (channel == null || channel.isClosed()) {
        closedCount++;
      }
    }
    if (closedCount == channels.length) {
      throw new ClosedException();
    } else {
      return selected;
    }
  }

  synchronized void schedule() {
    notify();
  }

  synchronized void closeChannel() {
    enabledChannels--;
    if (enabledChannels <= 0) {
      notify();
    }
  }
}
