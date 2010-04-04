package com.revolsys.parallel.tools;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeMap extends LinkedHashMap<String, Object> {

  public AttributeMap() {
  }

  public AttributeMap(
    final int initialCapacity,
    final float loadFactor,
    final boolean accessOrder) {
    super(initialCapacity, loadFactor, accessOrder);
  }

  public AttributeMap(
    final int initialCapacity,
    float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public AttributeMap(
    final int initialCapacity) {
    super(initialCapacity);
  }

  public AttributeMap(
    final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public Map<String, Object> getAttributes() {
    return this;
  }

  public void setAttributes(
    final Map<String, Object> attributes) {
    clear();
    putAll(attributes);
  }
}
