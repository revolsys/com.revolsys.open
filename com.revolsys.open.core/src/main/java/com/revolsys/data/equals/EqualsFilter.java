package com.revolsys.data.equals;

import java.util.Collection;
import java.util.Collections;

import com.revolsys.filter.Filter;

public class EqualsFilter<T> implements Filter<T> {
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
  public boolean accept(final T object) {
    if (equals.equals(this.object, object, excludeAttributes)) {
      return true;
    } else {
      return false;
    }
  }
}
