package com.revolsys.filter;

public class FilterAndValue<F, V> implements Filter<F> {
  private Filter<F> filter;

  private V value;

  public FilterAndValue(final Filter<F> filter, final V value) {
    this.filter = filter;
    this.value = value;
  }

  @Override
  public boolean accept(final F object) {
    return filter.accept(object);
  }

  public Filter<F> getFilter() {
    return filter;
  }

  public V getValue() {
    return value;
  }

  public void setFilter(final Filter<F> filter) {
    this.filter = filter;
  }

  public void setValue(final V value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "filter=" + filter + "\nvalue=" + value;
  }
}
