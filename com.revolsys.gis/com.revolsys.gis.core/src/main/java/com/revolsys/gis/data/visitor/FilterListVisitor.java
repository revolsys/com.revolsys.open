package com.revolsys.gis.data.visitor;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.filter.Filter;

public class FilterListVisitor<T> implements Visitor<T> {
  private final Filter<T> filter;

  private final List<T> results = new ArrayList<T>();

  public FilterListVisitor(
    final Filter<T> filter) {
    this.filter = filter;
  }

  public List<T> getResults() {
    return results;
  }

  @Override
  public String toString() {
    return filter.toString();
  }

  public boolean visit(
    final T item) {
    if (filter.accept(item)) {
      results.add(item);
    }
    return true;
  }
}
