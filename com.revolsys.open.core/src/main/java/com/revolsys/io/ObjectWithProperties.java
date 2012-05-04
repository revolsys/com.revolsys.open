package com.revolsys.io;

import java.util.Map;

public interface ObjectWithProperties {

  Map<String, Object> getProperties();

  <C> C getProperty(String name);

  void setProperties(final Map<String, Object> properties);

  void setProperty(String name, Object value);
}
