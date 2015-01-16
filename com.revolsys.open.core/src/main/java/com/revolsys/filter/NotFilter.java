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
    return !this.filter.accept(object);
  }

  public Filter<T> getFilter() {
    return this.filter;
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "NOT " + this.filter;
  }
}
