package com.revolsys.gis.data.io;

import java.util.Map;

import javax.xml.namespace.QName;

public interface ObjectWithProperties {

  /**
   * Get properties about the reader.
   * 
   * @return The properties.
   */
  Map<QName, Object> getProperties();

  /**
   * Get a property about the reader.
   * 
   * @param name The name of the property to get.
   * @return The property.
   */
  <C> C getProperty(
    QName name);

  void setProperty(QName name, Object value);
}
