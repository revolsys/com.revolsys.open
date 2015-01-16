package com.revolsys.io;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DelegatingObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new HashMap<String, Object>();

  private ObjectWithProperties object;

  public DelegatingObjectWithProperties() {
  }

  public DelegatingObjectWithProperties(final Object object) {
    if (object instanceof ObjectWithProperties) {
      this.object = (ObjectWithProperties)object;
    }
  }

  @Override
  public void clearProperties() {
    if (getObject() == null) {
      this.properties.clear();
    } else {
      getObject().clearProperties();
    }
  }

  protected void close() {
    this.properties = null;
    this.object = null;
  }

  @SuppressWarnings("unchecked")
  public <V extends ObjectWithProperties> V getObject() {
    return (V)this.object;
  }

  @Override
  public final Map<String, Object> getProperties() {
    if (getObject() == null) {
      return this.properties;
    } else {
      return getObject().getProperties();
    }
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

  @SuppressWarnings("unchecked")
  @Override
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
    this.object.removeProperty(propertyName);
    if (getObject() == null) {
      this.properties.remove(propertyName);
    } else {
      getObject().removeProperty(propertyName);
    }
  }

  protected void setObject(final ObjectWithProperties object) {
    this.object = object;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (getObject() == null) {
      this.properties.clear();
      this.properties.putAll(properties);
    } else {
      getObject().setProperties(properties);
    }
  }

  @Override
  public final void setProperty(final String name, final Object value) {
    if (getObject() == null) {
      this.properties.put(name, value);
    } else {
      getObject().setProperty(name, value);
    }
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

  @Override
  public String toString() {
    if (this.object == null) {
      return super.toString();
    } else {
      return this.object.toString();
    }
  }
}
