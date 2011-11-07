package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public class BaseInProcess<T> extends AbstractInProcess<T> {
  private boolean running = false;

  public BaseInProcess() {
  }

  public BaseInProcess(
    final Channel<T> in) {
    super(in);
  }

  public BaseInProcess(
    final int bufferSize) {
    super(bufferSize);
  }

  @Override
  protected final void run(
    final Channel<T> in) {
    running = true;
    try {
      preRun(in);
      while (running) {
        final T object = in.read();
        if (object != null) {
          process(in, object);
        }
      }
    } finally {
      try {
        postRun(in);
      } finally {
        running = false;
      }
    }
  }

  protected void process(
    final Channel<T> in,
    final T object) {
  }

  protected void preRun(
    final Channel<T> in) {
  }

  protected void postRun(
    final Channel<T> in) {
  }

}
