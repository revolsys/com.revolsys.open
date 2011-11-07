package com.revolsys.parallel.channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiInputSelector {
  private int enabledChannels = 0;

  private int guardEnabledChannels = 0;

  private long maxWait;

  private boolean scheduled;

  private Object monitor = new Object();

   void closeChannel() {
    synchronized (monitor) {
      enabledChannels--;
      if (enabledChannels <= 0) {
        monitor.notifyAll();
      }
    }
  }

  private int disableChannels(final List<? extends SelectableInput> channels) {
    int closedCount = 0;
    int selected = -1;
    for (int i = channels.size() - 1; i >= 0; i--) {
      final SelectableInput channel = channels.get(i);
      if (channel.disable()) {
        selected = i;
      } else if (channel.isClosed()) {
        closedCount++;
      }
    }
    if (closedCount == channels.size()) {
      throw new ClosedException();
    } else {
      return selected;
    }

  }

  private int disableChannels(final List<? extends SelectableInput> channels,
    final List<Boolean> guard) {
    int closedCount = 0;
    int selected = -1;
    for (int i = channels.size() - 1; i >= 0; i--) {
      final SelectableInput channel = channels.get(i);
      if (guard.get(i) && channel.disable()) {
        selected = i;
      } else if (channel == null || channel.isClosed()) {
        closedCount++;
      }
    }
    if (closedCount == channels.size()) {
      throw new ClosedException();
    } else {
      return selected;
    }
  }

  private boolean enableChannels(final List<? extends SelectableInput> channels) {
    enabledChannels = 0;
    scheduled = false;
    maxWait = Long.MAX_VALUE;
    int closedCount = 0;
    for (final SelectableInput channel : channels) {
      if (!channel.isClosed()) {
        if (channel.enable(this)) {
          enabledChannels++;
          return true;
        } else if (channel instanceof Timer) {
          final Timer timer = (Timer)channel;
          maxWait = Math.min(maxWait, timer.getWaitTime());
        }
      } else {
        closedCount++;
      }
    }
    return closedCount == channels.size();
  }

  private boolean enableChannels(
    final List<? extends SelectableInput> channels, final List<Boolean> guard) {
    enabledChannels = 0;
    scheduled = false;
    maxWait = Long.MAX_VALUE;
    int closedCount = 0;
    int activeChannelCount = 0;
    for (int i = 0; i < channels.size(); i++) {
      final SelectableInput channel = channels.get(i);
      if (guard.get(i)) {
        activeChannelCount++;
        if (!channel.isClosed()) {
          if (channel.enable(this)) {
            enabledChannels++;
            return true;
          } else if (channel instanceof Timer) {
            final Timer timer = (Timer)channel;
            maxWait = Math.min(maxWait, timer.getWaitTime());
          }
        } else {
          closedCount++;
        }
      }
    }
    guardEnabledChannels = activeChannelCount - closedCount;
    return closedCount == activeChannelCount;
  }

  void schedule() {
    synchronized (monitor) {
      scheduled = true;
      monitor.notifyAll();
    }
  }

  public synchronized int select(final List<? extends SelectableInput> channels) {
    return select(Long.MAX_VALUE, channels);
  }

  public synchronized int select(
    final List<? extends SelectableInput> channels, final boolean skip) {
    if (skip) {
      enableChannels(channels);
      return disableChannels(channels);
    } else {
      return select(channels);
    }
  }

  public synchronized int select(
    final List<? extends SelectableInput> channels, final List<Boolean> guard) {
    return select(channels, guard, Long.MAX_VALUE);
  }

  public synchronized int select(
    final List<? extends SelectableInput> channels, final List<Boolean> guard,
    final boolean skip) {
    if (skip) {
      enableChannels(channels, guard);
      return disableChannels(channels, guard);
    } else {
      return select(channels, guard);
    }
  }

  public synchronized int select(
    final List<? extends SelectableInput> channels, final List<Boolean> guard,
    final long msecs) {
    return select(channels, guard, msecs, 0);
  }

  public synchronized int select(
    final List<? extends SelectableInput> channels, final List<Boolean> guard,
    final long msecs, final int nsecs) {
    if (!enableChannels(channels, guard) && guardEnabledChannels > 0) {
      synchronized (monitor) {
        if (!scheduled) {
          try {
            monitor.wait(Math.min(msecs, maxWait), nsecs);
          } catch (final InterruptedException e) {
          }
        }
      }
    }
    return disableChannels(channels, guard);
  }

  public synchronized int select(final long msecs, final int nsecs,
    final List<? extends SelectableInput> channels) {
    if (!enableChannels(channels)) {
      if (msecs + nsecs >= 0) {
        synchronized (monitor) {
          try {
            if (!scheduled) {
              monitor.wait(Math.min(msecs, maxWait), nsecs);
            }
          } catch (final InterruptedException e) {
          }
        }
      }
    }
    return disableChannels(channels);
  }

  public synchronized int select(final long msecs, final int nsecs,
    final SelectableInput... channels) {
    return select(msecs, nsecs, Arrays.asList(channels));
  }

  public synchronized <T extends SelectableInput, R extends T> T selectChannelInput(
    final List<T> channels) {
    int index = select(Long.MAX_VALUE, channels);
    if (index == -1) {
      return null;
    } else {
      return channels.get(index);
    }
  }

  public synchronized int select(final long msecs,
    final List<? extends SelectableInput> channels) {
    return select(msecs, 0, channels);
  }

  public synchronized int select(final long msecs,
    final SelectableInput... channels) {
    return select(msecs, 0, channels);
  }

  public synchronized int select(final SelectableInput... channels) {
    return select(Long.MAX_VALUE, channels);
  }

  public synchronized int select(final SelectableInput[] channels,
    final boolean skip) {
    return select(Arrays.asList(channels), skip);
  }

  public synchronized int select(final SelectableInput[] channels,
    final boolean[] guard) {
    return select(channels, guard, Long.MAX_VALUE);
  }

  public synchronized int select(final SelectableInput[] channels,
    final boolean[] guard, final boolean skip) {
    List<Boolean> guardList = new ArrayList<Boolean>();
    for (boolean enabled : guard) {
      guardList.add(enabled);
    }
    return select(Arrays.asList(channels), guardList, skip);
  }

  public synchronized int select(final SelectableInput[] channels,
    final boolean[] guard, final long msecs) {
    return select(channels, guard, msecs, 0);
  }

  public synchronized int select(final SelectableInput[] channels,
    final boolean[] guard, final long msecs, final int nsecs) {
    List<Boolean> guardList = new ArrayList<Boolean>();
    for (boolean enabled : guard) {
      guardList.add(enabled);
    }
    return select(Arrays.asList(channels), guardList, msecs, nsecs);
  }

}
