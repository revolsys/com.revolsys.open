package com.revolsys.gis.model.data.equals;

import java.util.Collection;

public class StringEqualsIgnoreCase extends AbstractEquals<String> {
  protected boolean equalsNotNull(
    String object1,
    String object2,
    Collection<String> exclude) {
    return object1.equalsIgnoreCase(object2);
  }
}
