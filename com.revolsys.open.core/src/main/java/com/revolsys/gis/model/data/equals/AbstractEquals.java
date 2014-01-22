package com.revolsys.gis.model.data.equals;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;

public abstract class AbstractEquals<T> implements Equals<T> {
  private Reference<EqualsRegistry> equalsRegistry = new WeakReference<EqualsRegistry>(
    EqualsInstance.INSTANCE);

  @Override
  public boolean equals(final T object1, final T object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      return object2 == null;
    } else if (object2 == null) {
      return false;
    } else {
      return equalsNotNull(object1, object2, exclude);
    }
  }

  protected abstract boolean equalsNotNull(T object1, T object2,
    Collection<String> exclude);

  public EqualsRegistry getEqualsRegistry() {
    return equalsRegistry.get();
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = new WeakReference<EqualsRegistry>(equalsRegistry);
  }
}
