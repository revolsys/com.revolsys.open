package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public class BaseInOutProcess<T> extends AbstractInOutProcess<T> {
  private boolean running = false;

  @Override
  protected final void run(
    final Channel<T> in,
    final Channel<T> out) {
    running = true;
    try {
      preRun(in, out);
      while (running) {
        T object = in.read();
        if (object != null) {
          process(in, out, object);
        }
      }
    } finally {
      try {
        postRun(in, out);
      } finally {
        running = false;
      }
    }
  }

  protected void process(
    final Channel<T> in,
    final Channel<T> out,
    final T object) {
  }

  protected void preRun(
    final Channel<T> in,
    final Channel<T> out) {
  }

  protected void postRun(
    final Channel<T> in,
    final Channel<T> out) {
  }

}
