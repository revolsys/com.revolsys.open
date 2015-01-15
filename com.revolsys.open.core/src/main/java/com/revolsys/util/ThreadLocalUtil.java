package com.revolsys.util;

import com.revolsys.factory.Factory;

public class ThreadLocalUtil {

  public static <V> V get(final ThreadLocal<V> local, final Factory<V> factory) {
    V value = local.get();
    if (value == null) {
      value = factory.create();
      local.set(value);
    }
    return value;
  }

}
