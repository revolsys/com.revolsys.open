package com.revolsys.io;

import java.util.LinkedHashMap;
import java.util.Map;

public class NamedLinkedHashMap<K, V> extends LinkedHashMap<K, V> implements
  NamedObject {
  private static final long serialVersionUID = -874346734708399858L;

  private String name;

  public NamedLinkedHashMap(final String name) {
    this.name = name;
  }

  public NamedLinkedHashMap(final String name, Map<K, V> map) {
    super(map);
    this.name = name;
  }

  public NamedLinkedHashMap(final String name, K key, V value) {
    this.name = name;
    put(key, value);
  }

  public String getName() {
    return name;
  }
}
