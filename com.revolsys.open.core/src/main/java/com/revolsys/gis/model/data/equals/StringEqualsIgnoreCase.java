package com.revolsys.gis.model.data.equals;

import java.util.Collection;

public class StringEqualsIgnoreCase extends AbstractEquals<String> {
  @Override
  protected boolean equalsNotNull(final String object1, final String object2,
    final Collection<String> exclude) {
    return object1.equalsIgnoreCase(object2);
  }
}
