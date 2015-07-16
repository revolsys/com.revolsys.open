package com.revolsys.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class OrPredicate<T> implements Predicate<T> {
  private List<Predicate<T>> predicates = new ArrayList<>();

  public OrPredicate() {
  }

  public OrPredicate(final Collection<Predicate<T>> predicates) {
    this.predicates.addAll(predicates);
  }

  public OrPredicate(final Predicate<T>... predicates) {
    this(Arrays.asList(predicates));
  }

  public List<Predicate<T>> getPredicates() {
    return this.predicates;
  }

  public void setPredicates(final List<Predicate<T>> predicates) {
    this.predicates = predicates;
  }

  @Override
  public boolean test(final T object) {
    for (final Predicate<T> predicate : this.predicates) {
      if (predicate.test(object)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "OR" + this.predicates;
  }
}
