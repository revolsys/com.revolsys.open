package com.revolsys.io;

import java.util.Map;

public interface ObjectWithProperties {
  void clearProperties();

  Map<String, Object> getProperties();

  <C> C getProperty(String name);

  <C> C getProperty(String name, C defaultValue);

  void removeProperty(String propertyName);

  void setProperties(final Map<String, ? extends Object> properties);

  void setProperty(String name, Object value);

  void setPropertySoft(String name, Object value);

  void setPropertyWeak(String name, Object value);
}
