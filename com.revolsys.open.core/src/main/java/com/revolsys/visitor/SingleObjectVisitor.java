package com.revolsys.visitor;

import java.util.Comparator;

import java.util.function.Predicate;

public class SingleObjectVisitor<T> extends BaseVisitor<T> {
  private T object;

  public SingleObjectVisitor() {
  }

  public SingleObjectVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public SingleObjectVisitor(final Predicate<T> filter) {
    super(filter);
  }

  public SingleObjectVisitor(final Predicate<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  @Override
  public boolean doVisit(final T object) {
    if (this.object == null) {
      this.object = object;
    }
    return false;
  }

  public T getObject() {
    return this.object;
  }

  public void reset() {
    this.object = null;
  }
}
