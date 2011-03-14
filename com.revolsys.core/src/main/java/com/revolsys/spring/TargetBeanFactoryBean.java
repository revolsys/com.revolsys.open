package com.revolsys.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;

public class TargetBeanFactoryBean extends AbstractFactoryBean<Object> {

  private BeanFactory targetBeanFactory;

  private String targetBeanName;

  private BeanDefinition targetBeanDefinition;

  @Override
  protected Object createInstance() {
    return this.targetBeanFactory.getBean(this.targetBeanName);
  }

  @Override
  public Class<?> getObjectType() {
    return Object.class;
  }

  public BeanFactory getTargetBeanFactory() {
    return targetBeanFactory;
  }

  public String getTargetBeanName() {
    return targetBeanName;
  }

  public BeanDefinition getTargetBeanDefinition() {
    return targetBeanDefinition;
  }

  public void setTargetBeanDefinition(BeanDefinition targetBeanDefinition) {
    this.targetBeanDefinition = targetBeanDefinition;
  }

  public void setTargetBeanFactory(final BeanFactory targetBeanFactory) {
    this.targetBeanFactory = targetBeanFactory;
  }

  public void setTargetBeanName(final String targetBeanName) {
    this.targetBeanName = targetBeanName;
  }
}
