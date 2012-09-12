package com.revolsys.filter;

public class NotFilter<T> implements Filter<T> {
  private Filter<T> filter;

  public NotFilter() {
  }

  public NotFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  @Override
  public boolean accept(final T object) {
    return !filter.accept(object);
  }

  public Filter<T> getFilter() {
    return filter;
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "NOT " + filter;
  }
}
