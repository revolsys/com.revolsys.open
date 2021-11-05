package com.revolsys.collection.map;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jeometry.common.collection.map.AbstractDelegatingMap;

import com.revolsys.util.BaseCloneable;

public class DelegatingMap<K, V> extends AbstractDelegatingMap<K, V> implements Cloneable {

  public static <K2, V2> Map<K2, V2> newMap(final Map<K2, V2> map) {
    return new DelegatingMap<>(map);
  }

  private Map<K, V> map;

  public DelegatingMap() {
    this(new LinkedHashMap<K, V>());
  }

  public DelegatingMap(final Map<K, V> map) {
    super(true);
    if (map == null) {
      throw new IllegalArgumentException("Map cannot be null");
    }
    this.map = map;
  }

  @Override
  protected DelegatingMap<K, V> clone() {
    try {
      @SuppressWarnings("unchecked")
      final DelegatingMap<K, V> clone = (DelegatingMap<K, V>)super.clone();
      clone.map = BaseCloneable.clone(getMap());
      return clone;
    } catch (final CloneNotSupportedException e) {
      return this;
    }
  }

  @Override
  public Map<K, V> getMap() {
    return this.map;
  }

  public void setMap(final Map<K, V> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map cannot be null");
    }
    this.map = map;
  }

}
