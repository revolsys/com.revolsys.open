package com.revolsys.io;

import java.util.HashMap;
import java.util.Map;

public class DelegatingObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties;

  private ObjectWithProperties object;

  public DelegatingObjectWithProperties(final Object object) {
    if (object instanceof ObjectWithProperties) {
      this.object = (ObjectWithProperties)object;
    } else {
      properties = new HashMap<String, Object>();
    }
  }

  protected void close() {
    properties = null;
    object = null;
  }

  public final Map<String, Object> getProperties() {
    if (object == null) {
      return properties;
    } else {
      return object.getProperties();
    }
  }

  public <C> C getProperty(final String name) {
    if (object == null) {
      return (C)properties.get(name);
    } else {
      return (C)object.getProperty(name);
    }
  }

  public final void setProperty(final String name, final Object value) {
    if (object == null) {
      properties.put(name, value);
    } else {
      object.setProperty(name, value);
    }
  }

}
