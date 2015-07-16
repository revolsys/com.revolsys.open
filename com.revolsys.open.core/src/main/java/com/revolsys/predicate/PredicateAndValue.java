package com.revolsys.predicate;

import java.util.function.Predicate;

public class PredicateAndValue<F, V> implements Predicate<F> {
  private Predicate<F> predicate;

  private V value;

  public PredicateAndValue(final Predicate<F> predicate, final V value) {
    this.predicate = predicate;
    this.value = value;
  }

  public Predicate<F> getPredicate() {
    return this.predicate;
  }

  public V getValue() {
    return this.value;
  }

  public void setFilter(final Predicate<F> predicate) {
    this.predicate = predicate;
  }

  public void setValue(final V value) {
    this.value = value;
  }

  @Override
  public boolean test(final F object) {
    return this.predicate.test(object);
  }

  @Override
  public String toString() {
    return "predicate=" + this.predicate + "\nvalue=" + this.value;
  }
}
