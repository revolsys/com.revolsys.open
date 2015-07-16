package com.revolsys.visitor;

import java.util.Comparator;
import java.util.function.Predicate;

import com.revolsys.collection.Visitor;
import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.predicate.AndPredicate;
import com.revolsys.predicate.PredicateProxy;

public abstract class AbstractVisitor<T>
  implements Visitor<T>, PredicateProxy<T>, ComparatorProxy<T> {
  private Predicate<T> predicate;

  private Comparator<T> comparator;

  public AbstractVisitor() {
  }

  public AbstractVisitor(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public AbstractVisitor(final Predicate<T> predicate) {
    this.predicate = predicate;
  }

  public AbstractVisitor(final Predicate<T> predicate, final Comparator<T> comparator) {
    this.predicate = predicate;
    this.comparator = comparator;
  }

  @Override
  public Comparator<T> getComparator() {
    return this.comparator;
  }

  @Override
  public Predicate<T> getPredicate() {
    return this.predicate;
  }

  public void setComparator(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public void setFilter(final Predicate<T> predicate) {
    this.predicate = predicate;
  }

  public void setFilters(final Predicate<T>... predicates) {
    this.predicate = new AndPredicate<T>(predicates);
  }
}
