package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public interface InProcess<T> extends Process {
  Channel<T> getIn();

  InProcess<T> setIn(Channel<T> in);

  default InProcess<T> setIn(final OutProcess<T> process) {
    if (process != null) {
      final Channel<T> in = process.getOut();
      if (in != null) {
        setIn(in);
      }
    }
    return this;
  }
}
