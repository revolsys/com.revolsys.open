package com.revolsys.io;

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

  protected void close() {
    properties = null;
    object = null;
  }

  public ObjectWithProperties getObject() {
    return object;
  }

  @Override
  public final Map<String, Object> getProperties() {
    if (getObject() == null) {
      return properties;
    } else {
      return getObject().getProperties();
    }
  }

  @Override
  public <C> C getProperty(final String name) {
    if (getObject() == null) {
      return (C)properties.get(name);
    } else {
      return (C)getObject().getProperty(name);
    }
  }

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
  public void setProperties(final Map<String, Object> properties) {
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
      properties.put(name, value);
    } else {
      getObject().setProperty(name, value);
    }
  }
}
