package com.revolsys.gis.model.data.equals;

import java.util.Collection;

import org.springframework.util.StringUtils;

public class StringEqualsIgnoreCase extends AbstractEquals<String> {
  public static boolean equal(final String value1, final String value2) {
    if (StringUtils.hasText(value1)) {
      if (StringUtils.hasText(value2)) {
        return value1.equalsIgnoreCase(value2);
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
