package com.revolsys.io;

import java.util.HashMap;
import java.util.Map;

public class AbstractObjectWithProperties implements ObjectWithProperties {
  private final Map<String, Object> properties = new HashMap<String, Object>();

  public Map<String, Object> getProperties() {
    return properties;
  }

  public <C> C getProperty(final String name) {
    return (C)getProperties().get(name);
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties.clear();
    this.properties.putAll(properties);
  }

  public void setProperty(final String name, final Object value) {
    getProperties().put(name, value);
  }
}
