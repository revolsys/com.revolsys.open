package com.revolsys.io;

import java.util.HashMap;
import java.util.Map;

public class AbstractObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new HashMap<String, Object>();

  public Map<String, Object> getProperties() {
    return properties;
  }

  public <C> C getProperty(String name) {
    return (C)getProperties().get(name);
  }

  public void setProperty(String name, Object value) {
    getProperties().put(name, value);
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties.clear();
    this.properties.putAll(properties);
  }
}
