package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public class BaseInOutProcess<I, O> extends AbstractInOutProcess<I, O> {
  private boolean running = false;

  @Override
  protected final void run(final Channel<I> in, final Channel<O> out) {
    running = true;
    try {
      preRun(in, out);
      while (running) {
        I object = in.read();
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

  protected void process(final Channel<I> in, final Channel<O> out,
    final I object) {
  }

  protected void preRun(final Channel<I> in, final Channel<O> out) {
  }

  protected void postRun(final Channel<I> in, final Channel<O> out) {
  }

}
