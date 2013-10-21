package com.revolsys.io;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

public class AbstractObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  public AbstractObjectWithProperties() {
  }

  public AbstractObjectWithProperties(
    final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  @Override
  public void clearProperties() {
    properties.clear();
  }

  @PreDestroy
  public void close() {
    properties = null;
  }

  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> C getProperty(final String name) {
    final Map<String, Object> properties = getProperties();
    Object value = properties.get(name);
    if (value instanceof Reference) {
      final Reference<C> reference = (Reference<C>)value;
      if (reference.isEnqueued()) {
        value = null;
      } else {
        value = reference.get();
      }
      if (value == null) {
        properties.remove(name);
      }
    }
    return (C)value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> C getProperty(final String name, final C defaultValue) {
    final C value = (C)getProperty(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  @Override
  public void removeProperty(final String propertyName) {
    properties.remove(propertyName);
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        setProperty(name, value);
      }
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    final Map<String, Object> properties = getProperties();
    properties.put(name, value);
  }

  @Override
  public void setPropertySoft(final String name, final Object value) {
    final Map<String, Object> properties = getProperties();
    properties.put(name, new SoftReference<Object>(value));
  }

  @Override
  public void setPropertyWeak(final String name, final Object value) {
    final Map<String, Object> properties = getProperties();
    properties.put(name, new WeakReference<Object>(value));
  }
}
