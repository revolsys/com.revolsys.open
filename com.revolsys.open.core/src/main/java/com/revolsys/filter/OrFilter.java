package com.revolsys.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OrFilter<T> implements Filter<T> {
  private List<Filter<T>> filters = new ArrayList<Filter<T>>();

  public OrFilter() {
  }

  public OrFilter(final Collection<Filter<T>> filters) {
    this.filters.addAll(filters);
  }

  public OrFilter(final Filter<T>... filters) {
    this(Arrays.asList(filters));
  }

  @Override
  public boolean accept(final T object) {
    for (final Filter<T> filter : this.filters) {
      if (filter.accept(object)) {
        return true;
      }
    }
    return false;
  }

  public List<Filter<T>> getFilters() {
    return this.filters;
  }

  public void setFilters(final List<Filter<T>> filters) {
    this.filters = filters;
  }

  @Override
  public String toString() {
    return "OR" + this.filters;
  }
}
