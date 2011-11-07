package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public interface OutProcess<T> extends Process {

  Channel<T> getOut();

  void setOut(Channel<T> out);
}
