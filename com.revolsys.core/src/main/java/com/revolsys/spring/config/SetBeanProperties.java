package com.revolsys.spring.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.TypedStringValue;

public class SetBeanProperties implements BeanFactoryPostProcessor,
  InitializingBean {
  private Map<String, String> beanPropertyNames = new LinkedHashMap<String, String>();

  private String targetTypeName;

  private Object value;

  private String ref;

  private Object propertyValue;

  protected Object getPropertyValue() {
    return propertyValue;
  }

  protected void setPropertyValue(
    Object propertyValue) {
    this.propertyValue = propertyValue;
  }

  public void afterPropertiesSet()
    throws Exception {
    assert (value != null & ref != null) : "Cannot have a value and a ref";
    if (ref != null) {
      propertyValue = new RuntimeBeanNameReference(ref);
    } else if (value != null) {
      if (value instanceof String) {
        if (targetTypeName == null) {
          propertyValue = new TypedStringValue((String)value);
        } else {
          propertyValue = new TypedStringValue((String)value, targetTypeName);
        }
      } else {
        propertyValue = value;
      }
    }
  }

  public void postProcessBeanFactory(
    ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    for (Entry<String, String> beanPropertyName : beanPropertyNames.entrySet()) {
      String beanName = beanPropertyName.getKey();
      String[] aliases = beanFactory.getAliases(beanName);
      if (aliases.length > 0) {
        beanName = aliases[0];
      }
      String propertyName = beanPropertyName.getValue();
      BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      beanDefinition.setLazyInit(false);
      final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
      propertyValues.add(propertyName, propertyValue);
    }
  }

  public Map<String, String> getBeanPropertyNames() {
    return beanPropertyNames;
  }

  public void addBeanPropertyName(
    final String beanName,
    final String propertyName) {
    beanPropertyNames.put(beanName, propertyName);
  }

  public void setBeanPropertyNames(
    Map<String, String> beanPropertyNames) {
    this.beanPropertyNames = beanPropertyNames;
  }

  public String getTargetTypeName() {
    return targetTypeName;
  }

  public void setTargetTypeName(
    String targetTypeName) {
    this.targetTypeName = targetTypeName;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(
    Object value) {
    this.value = value;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(
    String ref) {
    this.ref = ref;
  }
}
