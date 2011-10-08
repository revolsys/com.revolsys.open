package com.revolsys.collection;

import java.util.UUID;
import java.util.WeakHashMap;

public class WeakUuidObjectMap extends WeakHashMap<UUID, Object> {
  private static WeakUuidObjectMap INSTANCE = new WeakUuidObjectMap();

  @SuppressWarnings("unchecked")
  public static <T> T getObject(UUID uuid) {
    return (T)INSTANCE.get(uuid);
  }

  public static void putObject(UUID uuid, Object value) {
    INSTANCE.put(uuid, value);
  }
}
