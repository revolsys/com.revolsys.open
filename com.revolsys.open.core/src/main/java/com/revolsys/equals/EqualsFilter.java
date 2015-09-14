package com.revolsys.equals;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class EqualsFilter<T> implements Predicate<T> {
  private Equals<Object> equals = EqualsInstance.INSTANCE;

  private Collection<String> excludeAttributes = Collections.emptyList();

  private final T object;

  public EqualsFilter(final Equals<Object> equals, final T object) {
    this.equals = equals;
    this.object = object;
  }

  public EqualsFilter(final Equals<Object> equals, final T object,
    final Collection<String> excludeAttributes) {
    this.equals = equals;
    this.object = object;
    this.excludeAttributes = excludeAttributes;
  }

  public EqualsFilter(final T object) {
    this.object = object;
  }

  public EqualsFilter(final T object, final Collection<String> excludeAttributes) {
    this.object = object;
    this.excludeAttributes = excludeAttributes;
  }

  @Override
  public boolean test(final T object) {
    if (this.equals.equals(this.object, object, this.excludeAttributes)) {
      return true;
    } else {
      return false;
    }
  }
}
