package com.revolsys.parallel.channel;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;

public class ChannelInputIterator<T> extends AbstractIterator<T> {
  private ChannelInput<T> in;

  public ChannelInputIterator(ChannelInput<T> in) {
    this.in = in;
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    try {
      T object = in.read();
      return object;
    } catch (ClosedException e) {
      throw new NoSuchElementException();
    }
  }
}
