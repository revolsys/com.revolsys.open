package com.revolsys.gis.model.data.equals;

import java.util.Collection;

public abstract class AbstractEquals<T> implements Equals<T> {
  private final EqualsRegistry equalsRegistry = EqualsRegistry.INSTANCE;

  public boolean equals(
    final T object1,
    final T object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      return object2 == null;
    } else if (object2 == null) {
      return false;
    } else {
      return equalsNotNull(object1, object2, exclude);
    }
  }

  protected abstract boolean equalsNotNull(
    T object1,
    T object2,
    Collection<String> exclude);

  public EqualsRegistry getEqualsRegistry() {
    return equalsRegistry;
  }

  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
