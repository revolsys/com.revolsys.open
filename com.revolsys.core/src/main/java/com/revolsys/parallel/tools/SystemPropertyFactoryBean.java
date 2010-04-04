package com.revolsys.parallel.tools;

import org.springframework.beans.factory.FactoryBean;

public class SystemPropertyFactoryBean implements FactoryBean<String> {
  private String name;

  private String defaultValue;

  public String getObject()
    throws Exception {
    final String propertyValue = System.getProperty(name);
    if (propertyValue == null) {
      return defaultValue;
    } else {
      return propertyValue;
    }
  }

  public Class<?> getObjectType() {
    return String.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public String getName() {
    return name;
  }

  public void setName(
    String name) {
    this.name = name;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(
    String defaultValue) {
    this.defaultValue = defaultValue;
  }
}
