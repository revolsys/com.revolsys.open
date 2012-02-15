package com.revolsys.util;

import javax.annotation.PostConstruct;

public class SetPropertyValue {

  private Object bean;

  private String propertyName;

  private Object value;

  public Object getBean() {
    return bean;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public Object getValue() {
    return value;
  }

  @PostConstruct
  public void init() {
    if (bean != null && propertyName != null) {
      JavaBeanUtil.setProperty(bean, propertyName, value);
    }
  }

  public void setBean(final Object bean) {
    this.bean = bean;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

}
