package com.revolsys.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class TargetBeanFactoryBean extends AbstractFactoryBean<Object> {

  private BeanFactory targetBeanFactory;

  private String targetBeanName;

  @Override
  protected Object createInstance() {
    return this.targetBeanFactory.getBean(this.targetBeanName);
  }

  @Override
  public Class getObjectType() {
    return ObjectFactory.class;
  }

  public BeanFactory getTargetBeanFactory() {
    return targetBeanFactory;
  }

  public String getTargetBeanName() {
    return targetBeanName;
  }

  public void setTargetBeanFactory(
    final BeanFactory targetBeanFactory) {
    this.targetBeanFactory = targetBeanFactory;
  }

  public void setTargetBeanName(
    final String targetBeanName) {
    this.targetBeanName = targetBeanName;
  }
}
