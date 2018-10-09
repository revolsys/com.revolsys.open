package com.revolsys.parallel.process;

import com.revolsys.parallel.ThreadUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.util.Cancellable;

public class BaseInProcess<T> extends AbstractInProcess<T> implements Cancellable {
  private boolean running = false;

  private Cancellable cancellable;

  public BaseInProcess() {
  }

  public BaseInProcess(final Channel<T> in) {
    super(in);
  }

  public BaseInProcess(final int bufferSize) {
    super(bufferSize);
  }

  public BaseInProcess(final String processName) {
    super(processName);
  }

  @Override
  public boolean isCancelled() {
    if (this.running) {
      if (this.cancellable != null) {
        return this.cancellable.isCancelled();
      } else {
        return false;
      }
    }
    return true;
  }

  protected void postRun(final Channel<T> in) {
  }

  protected void preRun(final Channel<T> in) {
  }

  protected void process(final Channel<T> in, final T object) {
  }

  @Override
  protected void run(final Channel<T> in) {
    this.running = true;
    try {
      preRun(in);
      while (!isCancelled()) {
        if (ThreadUtil.isInterrupted()) {
          return;
        } else {
          final T object = in.read(5000);
          if (ThreadUtil.isInterrupted()) {
            return;
          } else if (object != null) {
            process(in, object);
          }
        }
      }
    } finally {
      try {
        postRun(in);
      } finally {
        this.running = false;
      }
    }
  }

  public BaseInProcess<T> setCancellable(final Cancellable cancellable) {
    this.cancellable = cancellable;
    return this;
  }

}
