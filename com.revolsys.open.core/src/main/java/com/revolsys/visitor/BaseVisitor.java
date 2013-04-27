package com.revolsys.visitor;

import java.util.Comparator;

import com.revolsys.filter.Filter;

public class BaseVisitor<T> extends AbstractVisitor<T> {

  public BaseVisitor() {
  }

  public BaseVisitor(Comparator<T> comparator) {
    super(comparator);
  }

  public BaseVisitor(Filter<T> filter, Comparator<T> comparator) {
    super(filter, comparator);
  }

  public BaseVisitor(Filter<T> filter) {
    super(filter);
  }

  @Override
  public boolean visit(T object) {
    Filter<T> filter = getFilter();
    if (filter == null || filter.accept(object)) {
      return doVisit(object);
    } else {
      return true;
    }
  }

  protected boolean doVisit(T object) {
    return true;
  }
}
