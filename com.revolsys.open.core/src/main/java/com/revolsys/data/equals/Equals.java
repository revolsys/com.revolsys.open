package com.revolsys.data.equals;

import java.util.Collection;

public interface Equals<T> {
  boolean equals(T object1, T object2, Collection<String> exclude);

  void setEqualsRegistry(EqualsRegistry equalsRegistry);
}
