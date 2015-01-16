package com.revolsys.util;

import javax.annotation.PostConstruct;

public class SetPropertyValue {

  private Object bean;

  private String propertyName;

  private Object value;

  public Object getBean() {
    return this.bean;
  }

  public String getPropertyName() {
    return this.propertyName;
  }

  public Object getValue() {
    return this.value;
  }

  @PostConstruct
  public void init() {
    if (this.bean != null && this.propertyName != null) {
      JavaBeanUtil.setProperty(this.bean, this.propertyName, this.value);
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
