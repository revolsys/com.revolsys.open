package com.revolsys.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.revolsys.collection.map.Maps;
import com.revolsys.util.Exceptions;

public class BaseObjectWithProperties implements ObjectWithProperties {
  private Map<String, Object> properties = new LinkedHashMap<>();

  public BaseObjectWithProperties() {
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

}
