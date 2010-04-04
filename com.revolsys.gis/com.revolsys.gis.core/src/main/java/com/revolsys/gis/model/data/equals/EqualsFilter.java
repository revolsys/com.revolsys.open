package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Collections;

import com.revolsys.filter.Filter;

public class EqualsFilter<T> implements Filter<T> {
  private Collection<String> excludeAttributes = Collections.emptyList();

  private final T object;

  public EqualsFilter(
    final T object) {
    this.object = object;
  }

  public EqualsFilter(
    final T object,
    final Collection<String> excludeAttributes) {
    this.object = object;
    this.excludeAttributes = excludeAttributes;
  }

  public boolean accept(
    final T object) {
    if (EqualsRegistry.INSTANCE.equals(this.object, object, excludeAttributes)) {
      return true;
    } else {
      return false;
    }
  }
}
