package com.revolsys.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;

public class TargetBeanFactoryBean extends AbstractFactoryBean<Object> {

  private BeanFactory targetBeanFactory;

  private String targetBeanName;

  private BeanDefinition targetBeanDefinition;

  private Class<?> targetBeanClass;

  public TargetBeanFactoryBean() {
  }

  @Override
  protected Object createInstance() {
    return this.targetBeanFactory.getBean(this.targetBeanName);
  }

  @Override
  public Class<?> getObjectType() {
    if (targetBeanClass == null) {
      return Object.class;
    } else {
      return targetBeanClass;
    }
  }

  public Class<?> getTargetBeanClass() {
    return targetBeanClass;
  }

  public BeanDefinition getTargetBeanDefinition() {
    return targetBeanDefinition;
  }

  public BeanFactory getTargetBeanFactory() {
    return targetBeanFactory;
  }

  public String getTargetBeanName() {
    return targetBeanName;
  }

  public void setTargetBeanClass(final Class<?> targetBeanClass) {
    this.targetBeanClass = targetBeanClass;
  }

  public void setTargetBeanDefinition(final BeanDefinition targetBeanDefinition) {
    this.targetBeanDefinition = targetBeanDefinition;
  }

  public void setTargetBeanFactory(final BeanFactory targetBeanFactory) {
    this.targetBeanFactory = targetBeanFactory;
  }

  public void setTargetBeanName(final String targetBeanName) {
    this.targetBeanName = targetBeanName;
  }

}
