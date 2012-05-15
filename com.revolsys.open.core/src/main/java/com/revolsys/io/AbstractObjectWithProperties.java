package com.revolsys.io;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

public class AbstractObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new HashMap<String, Object>();

  @PreDestroy
  public void close() {
    properties = null;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public <C> C getProperty(final String name) {
    return (C)getProperties().get(name);
  }

  @SuppressWarnings("unchecked")
  public <C> C getProperty(final String name, C defaultValue) {
    C value = (C)getProperties().get(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties.clear();
    if (properties != null) {
      this.properties.putAll(properties);
    }
  }

  public void setProperty(final String name, final Object value) {
    getProperties().put(name, value);
  }
}
