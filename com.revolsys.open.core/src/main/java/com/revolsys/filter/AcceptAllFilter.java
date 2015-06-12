package com.revolsys.filter;

public class AcceptAllFilter<T> implements Filter<T> {

  private static final Filter<?> ACCEPT_ALL_FILTER = new AcceptAllFilter<>();

  @SuppressWarnings("unchecked")
  public static <V> Filter<V> get() {
    return (Filter<V>)ACCEPT_ALL_FILTER;
  }

  @Override
  public boolean accept(final T object) {
    return true;
  }
}
