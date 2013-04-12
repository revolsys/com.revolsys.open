package com.revolsys.filter;

public class AcceptAllFilter<T> implements Filter<T> {
  @Override
  public boolean accept(final T object) {
    return true;
  }
}
