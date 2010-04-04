package com.revolsys.parallel.channel;

public interface AltingChannelInput {
  int EMPTY = 0;
  int AVAILABLE = 1;
  int CLOSED = 2;
  boolean enable(MultiChannelReadSelector alt);

  boolean disable();
  
  boolean isClosed();
}
