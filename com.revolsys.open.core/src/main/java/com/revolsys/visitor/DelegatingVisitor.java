package com.revolsys.visitor;

import java.util.Comparator;
import java.util.function.Predicate;

import com.revolsys.collection.Visitor;

public class DelegatingVisitor<T> extends AbstractVisitor<T> {
  private Visitor<T> visitor;

  public DelegatingVisitor() {
  }

  public DelegatingVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public DelegatingVisitor(final Comparator<T> comparator, final Visitor<T> visitor) {
    super(comparator);
    this.visitor = visitor;
  }

  public DelegatingVisitor(final Predicate<T> filter) {
    super(filter);
  }

  public DelegatingVisitor(final Predicate<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  public DelegatingVisitor(final Predicate<T> filter, final Comparator<T> comparator,
    final Visitor<T> visitor) {
    super(filter, comparator);
    this.visitor = visitor;
  }

  public DelegatingVisitor(final Predicate<T> filter, final Visitor<T> visitor) {
    super(filter);
    this.visitor = visitor;
  }

  public DelegatingVisitor(final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  @Override
  public void accept(final T item) {
    final Predicate<T> predicate = getPredicate();
    if (predicate.test(item)) {
      this.visitor.accept(item);
    }
  }

  public Visitor<T> getVisitor() {
    return this.visitor;
  }

  public void setVisitor(final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  @Override
  public String toString() {
    return this.visitor.toString();
  }
}
