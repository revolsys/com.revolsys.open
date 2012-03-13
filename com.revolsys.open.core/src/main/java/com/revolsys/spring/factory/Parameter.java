package com.revolsys.spring.factory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class Parameter implements FactoryBean<Object> {

  public static void registerBeanDefinition(
    final BeanDefinitionRegistry registry,
    final String beanName,
    Object value) {
    final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClass(Parameter.class);
    final MutablePropertyValues values = beanDefinition.getPropertyValues();
    values.add("value", value);
    registry.registerBeanDefinition(beanName, beanDefinition);
  }

  public static void registerBeanDefinition(
    final BeanDefinitionRegistry registry,
    final BeanFactory beanFactory,
    final String beanName,
    final String alias) {
    if (beanFactory.containsBean(beanName)) {
      Object value = beanFactory.getBean(beanName);
      registerBeanDefinition(registry, alias, value);
    }
  }

  public static void registerBeanDefinition(
    final BeanDefinitionRegistry registry,
    final BeanFactory beanFactory,
    final String beanName) {
    registerBeanDefinition(registry, beanFactory, beanName, beanName);
  }

  private Class<?> type;

  private Object value;

  public Parameter() {
  }

  public Parameter(Object value) {
    this.value = value;
  }

  public Object getObject() throws Exception {
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

  public void setType(final Class type) {
    this.type = type;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

}
