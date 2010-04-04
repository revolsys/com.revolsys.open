package com.revolsys.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OrFilter<T> implements Filter<T> {
  private final List<Filter<T>> filters = new ArrayList<Filter<T>>();

  public OrFilter(
    final Collection<Filter<T>> filters) {
    this.filters.addAll(filters);
  }

  public OrFilter(
    final Filter<T>... filters) {
    this(Arrays.asList(filters));
  }

  public boolean accept(
    final T object) {
    for (final Filter<T> filter : filters) {
      if (filter.accept(object)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "OR" + filters;
  }
}
