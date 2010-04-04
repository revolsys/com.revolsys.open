package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public interface InProcess<T> extends Process {
  Channel<T> getIn();

  void setIn(Channel<T> in);

}
