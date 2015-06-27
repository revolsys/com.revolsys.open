package com.revolsys.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

public class BaseObjectWithProperties implements ObjectWithProperties {
  private final Map<String, Object> properties = new LinkedHashMap<String, Object>();

  public BaseObjectWithProperties() {
  }

  public BaseObjectWithProperties(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  @Override
  @PreDestroy
  public void close() {
    clearProperties();
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.properties;
  }
}
