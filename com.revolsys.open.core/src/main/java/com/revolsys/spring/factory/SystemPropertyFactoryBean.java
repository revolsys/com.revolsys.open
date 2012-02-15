package com.revolsys.spring.factory;

import org.springframework.beans.factory.FactoryBean;

public class SystemPropertyFactoryBean implements FactoryBean<String> {
  private String name;

  private String defaultValue;

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getName() {
    return name;
  }

  public String getObject() throws Exception {
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

  public void setDefaultValue(final String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
