package com.revolsys.gis.data.visitor;

import com.revolsys.filter.Filter;

public class FilterVisitor<T> extends NestedVisitor<T> {
  private final Filter<T> filter;

  public FilterVisitor(
    final Filter<T> filter,
    final Visitor<T> visitor) {
    super(visitor);
    this.filter = filter;
  }

  @Override
  public boolean visit(
    final T item) {
    if (filter.accept(item)) {
      return super.visit(item);
    } else {
      return true;
    }
  }

}
