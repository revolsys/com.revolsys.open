package com.revolsys.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

public class BeanReferenceListFactoryBean<T> implements FactoryBean<List<T>>,
  BeanFactoryAware {

  private List<String> beanNames = new ArrayList<String>();

  private BeanFactory beanFactory;

  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  public List<String> getBeanNames() {
    return beanNames;
  }

  public void setBeanNames(List<String> beanNames) {
    this.beanNames = beanNames;
  }

  @SuppressWarnings("unchecked")
  public List<T> getObject() throws Exception {
    List<T> beans = new ArrayList<T>();
    for (int i = 0; i < beanNames.size(); i++) {
      final String beanName = beanNames.get(i);
      T bean = (T)beanFactory.getBean(beanName);
      beans.add(bean);
    }
    return beans;
  }

  public Class<?> getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
