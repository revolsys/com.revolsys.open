package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public interface OutProcess<T> extends Process {

  Channel<T> getOut();

  void setOut(Channel<T> out);

  default void setOut(final InProcess<T> process) {
    if (process != null) {
      final Channel<T> channel = process.getIn();
      if (channel != null) {
        setOut(channel);
      }
    }
  }
}
