package com.revolsys.filter;

public class NotFilter<T> implements Filter<T> {
  private final Filter<T> filter;

  public NotFilter(
    final Filter<T> filter) {
    this.filter = filter;
  }

  public boolean accept(
    final T object) {
    return !filter.accept(object);
  }

  @Override
  public String toString() {
    return "NOT " + filter;
  }
}
