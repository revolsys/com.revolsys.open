package com.revolsys.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.revolsys.beans.AbstractPropertyChangeSupportProxy;
import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.datatype.DataType;
import com.revolsys.util.Property;

public class BaseObjectWithPropertiesAndChange extends AbstractPropertyChangeSupportProxy
  implements ObjectWithProperties {
  private final Map<String, Object> properties = new LinkedHashMap<>();

  public BaseObjectWithPropertiesAndChange() {
  }

  public BaseObjectWithPropertiesAndChange(final Map<String, ? extends Object> properties) {
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

  @Override
  public <C> C getProperty(final String name) {
    C value = Property.getSimple(this, name);
    if (value == null) {
      final Map<String, Object> properties = getProperties();
      value = ObjectWithProperties.getProperty(this, properties, name);
    }
    return value;
  }

  @Override
  public void setProperty(final String name, final Object value) {
    final Object oldValue = getProperty(name);
    if (!DataType.equal(oldValue, value)) {
      if (!Property.setSimple(this, name, value)) {
        final Map<String, Object> properties = getProperties();
        properties.put(name, value);
      }
      final Object newValue = getProperty(name);
      final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(this, "property",
        oldValue, newValue, name);
      firePropertyChange(event);
    }
  }
}
