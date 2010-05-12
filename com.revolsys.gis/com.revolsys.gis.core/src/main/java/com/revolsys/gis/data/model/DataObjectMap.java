package com.revolsys.gis.data.model;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataObjectMap extends AbstractMap<String, Object> {
  private DataObject object;

  private LinkedHashSet<Entry<String, Object>> entries;

  public DataObjectMap() {
  }

  public DataObjectMap(
    final DataObject object) {
    this.object = object;
  }

  @Override
  public void clear() {
    throw new IllegalArgumentException("Cannot clear a data object map");
  }

  @Override
  public boolean containsKey(
    final Object name) {
    return object.getMetaData().hasAttribute(name.toString());
  }

  @Override
  public boolean containsValue(
    final Object value) {
    if (value != null) {
      for (int i = 0; i < size(); i++) {
        final Object objectValue = object.getValue(i);
        if (objectValue != null) {
          if (objectValue == value || objectValue.equals(value)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    if (entries == null) {
      entries = new LinkedHashSet<Entry<String, Object>>();
      for (int i = 0; i < size(); i++) {
        final DataObjectMapEntry entry = new DataObjectMapEntry(object, i);
        entries.add(entry);
      }
    }
    return entries;
  }

  @Override
  public Object get(
    final Object key) {
    return object.getValue(key.toString());
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Set<String> keySet() {
    return new LinkedHashSet<String>(object.getMetaData().getAttributeNames());
  }

  @Override
  public Object put(
    final String key,
    final Object value) {
    object.setValue(key, value);
    return value;
  }

  @Override
  public void putAll(
    final Map<? extends String, ? extends Object> values) {
    for (final Entry<? extends String, ? extends Object> entry : values.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      put(key, value);
    }
  }

  @Override
  public Object remove(
    final Object key) {
    final Object value = get(key);
    object.setValue(key.toString(), null);
    return value;
  }

  public void setObject(
    final DataObject object) {
    this.object = object;
  }

  @Override
  public int size() {
    return object.getMetaData().getAttributeCount();
  }

  @Override
  public Collection<Object> values() {
    return object.getValues();
  }

}
