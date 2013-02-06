package com.revolsys.filter;

public class AcceptAllFilter<T> implements Filter<T> {
  @Override
  public boolean accept(T object) {
    return true;
  }
}
