package com.revolsys.data.equals;

import java.util.Collection;

public class BooleanEquals implements Equals<Object> {
  public static boolean getBoolean(final Object object1) {
    if (object1 == null) {
      return false;
    } else if (object1 instanceof Boolean) {
      return (Boolean)object1;
    } else {
      final String string = object1.toString();
      if (string.equals("1")) {
        return true;
      } else if (string.equals("Y")) {
        return true;
      } else {
        return Boolean.parseBoolean(string);
      }
    }
  }

  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    final boolean boolean1 = getBoolean(object1);
    final boolean boolean2 = getBoolean(object2);
    return boolean1 == boolean2;
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
