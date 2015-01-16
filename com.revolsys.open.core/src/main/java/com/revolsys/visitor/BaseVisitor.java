package com.revolsys.visitor;

import java.util.Comparator;

import com.revolsys.filter.Filter;

public class BaseVisitor<T> extends AbstractVisitor<T> {

  public BaseVisitor() {
  }

  public BaseVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public BaseVisitor(final Filter<T> filter) {
    super(filter);
  }

  public BaseVisitor(final Filter<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  protected boolean doVisit(final T object) {
    return true;
  }

  @Override
  public boolean visit(final T object) {
    final Filter<T> filter = getFilter();
    if (filter == null || filter.accept(object)) {
      return doVisit(object);
    } else {
      return true;
    }
  }
}
