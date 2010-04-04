package com.revolsys.io;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public abstract class AbstractMapWriter extends
  AbstractWriter<Map<String, ? extends Object>> implements MapWriter {

  private Map<QName, Object> properties = new HashMap<QName, Object>();

  public void close() {
  }

  public void flush() {
  }

  @SuppressWarnings("unchecked")
  public <V> V getProperty(
    final QName name) {
    return (V)properties.get(name);
  }

  public void setProperty(
    QName name,
    Object value) {
    properties.put(name, value);

  }
}
