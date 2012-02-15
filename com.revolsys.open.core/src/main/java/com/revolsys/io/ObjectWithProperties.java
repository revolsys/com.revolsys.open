package com.revolsys.io;

import java.util.Map;

public interface ObjectWithProperties {

  /**
   * Get properties about the reader.
   * 
   * @return The properties.
   */
  Map<String, Object> getProperties();

  /**
   * Get a property about the reader.
   * 
   * @param name The name of the property to get.
   * @return The property.
   */
  <C> C getProperty(String name);

  void setProperty(String name, Object value);
}
