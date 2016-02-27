package com.revolsys.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import com.revolsys.collection.map.Maps;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

public class BaseObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new LinkedHashMap<>();

  public BaseObjectWithProperties() {
  }

  public BaseObjectWithProperties(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  @Override
  protected BaseObjectWithProperties clone() {
    try {
      final BaseObjectWithProperties clone = (BaseObjectWithProperties)super.clone();
      clone.properties = Maps.newLinkedHash(this.properties);
      return clone;
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
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
    try {
      if (!Property.setSimple(this, name, value)) {
        final Map<String, Object> properties = getProperties();
        properties.put(name, value);
      }
    } catch (final Throwable e) {
      setPropertyError(name, value, e);
    }
  }

  protected void setPropertyError(final String name, final Object value, final Throwable e) {
    final Logger logger = Logger.getLogger(getClass());
    if (logger.isDebugEnabled()) {
      logger.debug("Error setting " + name + '=' + value, e);
    }
  }
}
