package com.revolsys.io;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

public class AbstractObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new HashMap<String, Object>();

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
    Map<String, Object> properties = getProperties();
    Object value = properties.get(name);
    if (value instanceof Reference) {
      Reference<C> reference = (Reference<C>)value;
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
  public void setProperties(final Map<String, Object> properties) {
    Map<String, Object> oldProperties = getProperties();
    oldProperties.clear();
    if (properties != null) {
      oldProperties.putAll(properties);
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    Map<String, Object> properties = getProperties();
    properties.put(name, value);
  }

  public void setPropertySoft(final String name, final Object value) {
    Map<String, Object> properties = getProperties();
    properties.put(name, new SoftReference<Object>(value));
  }

  public void setPropertyWeak(final String name, final Object value) {
    Map<String, Object> properties = getProperties();
    properties.put(name, new WeakReference<Object>(value));
  }
}
