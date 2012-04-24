package com.revolsys.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class BeanReferenceListFactoryBean<T> extends
  AbstractFactoryBean<List<T>> {

  private List<String> beanNames = new ArrayList<String>();

  public List<String> getBeanNames() {
    return beanNames;
  }

  @Override
  protected List<T> createInstance() throws Exception {
    BeanFactory beanFactory = getBeanFactory();
    final List<T> beans = new ArrayList<T>();
    for (int i = 0; i < beanNames.size(); i++) {
      final String beanName = beanNames.get(i);
      final T bean = (T)beanFactory.getBean(beanName);
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

  public void setBeanNames(final List<String> beanNames) {
    this.beanNames = beanNames;
  }

}
