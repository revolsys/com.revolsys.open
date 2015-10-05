package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public interface InProcess<T> extends Process {
  Channel<T> getIn();

  void setIn(Channel<T> in);

  default void setIn(final OutProcess<T> process) {
    if (process != null) {
      final Channel<T> in = process.getOut();
      if (in != null) {
        setIn(in);
      }
    }
  }
}
