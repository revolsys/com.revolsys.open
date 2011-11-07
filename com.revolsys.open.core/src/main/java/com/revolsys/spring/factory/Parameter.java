package com.revolsys.spring.factory;

import org.springframework.beans.factory.FactoryBean;

public class Parameter implements FactoryBean<Object> {
  private Class<?> type;

  private Object value;

  public Object getObject()
    throws Exception {
    return value;
  }

  public Class getObjectType() {
    if (type == null && value != null) {
      return value.getClass();
    } else {
      return type;
    }
  }

  public Class getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setType(
    Class type) {
    this.type = type;
  }

  public void setValue(
    Object value) {
    this.value = value;
  }

}
