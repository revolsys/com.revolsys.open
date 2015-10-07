package com.revolsys.equals;

import java.util.Arrays;
import java.util.Collection;

public interface Equals<T> {
  static boolean equal(final Object object1, final Object object2) {
    return EqualsInstance.INSTANCE.equals(object1, object2);
  }

  static boolean equal(final Object object1, final Object object2,
    final Collection<String> exclude) {
    return EqualsInstance.INSTANCE.equals(object1, object2, exclude);
  }

  static boolean equal(final Object object1, final Object object2, final String... exclude) {
    return equal(object1, object2, Arrays.asList(exclude));
  }

  boolean equals(T object1, T object2, Collection<String> exclude);

  default void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
