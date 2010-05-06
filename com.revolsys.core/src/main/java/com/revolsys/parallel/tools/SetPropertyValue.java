package com.revolsys.parallel.tools;

import javax.annotation.PostConstruct;

import com.revolsys.util.JavaBeanUtil;

public class SetPropertyValue {

  private Object bean;

  private String propertyName;

  private Object value;

  public Object getBean() {
    return bean;
  }

  public void setBean(
    Object bean) {
    this.bean = bean;
  }

  @PostConstruct
  public void init() {
    if (bean != null && propertyName != null) {
      JavaBeanUtil.executeSetMethod(bean, propertyName, value);
    }
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(
    String propertyName) {
    this.propertyName = propertyName;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(
    Object value) {
    this.value = value;
  }

}
