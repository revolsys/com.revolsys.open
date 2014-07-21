package com.revolsys.data.equals;

import java.util.Collection;

import com.revolsys.util.Property;

public class StringEqualsIgnoreCase extends AbstractEquals<String> {
  public static boolean equal(final String value1, final String value2) {
    if (Property.hasValue(value1)) {
      if (Property.hasValue(value2)) {
        return value1.trim().equalsIgnoreCase(value2.trim());
      }
    }
    return false;
  }

  @Override
  protected boolean equalsNotNull(final String object1, final String object2,
    final Collection<String> exclude) {
    return object1.equalsIgnoreCase(object2);
  }
}
