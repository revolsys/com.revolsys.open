package com.revolsys.io;

import java.util.LinkedHashMap;
import java.util.Map;

public class NamedLinkedHashMap<K, V> extends LinkedHashMap<K, V> implements
  NamedObject {

  private String name;

  public NamedLinkedHashMap(
    final String name) {
    this.name = name;
  }

  public NamedLinkedHashMap(
    final String name,
    Map map) {
    super(map);
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
