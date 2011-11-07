package com.revolsys.ui.web.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class BeanReference implements BeanFactoryAware {
  private String bean;

  private String name;

  private BeanFactory factory;

  /**
   * @return Returns the bean.
   */
  public String getBean() {
    return bean;
  }

  /**
   * @param bean The bean to set.
   */
  public void setBean(final String bean) {
    this.bean = bean;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  public Object getReferencedBean() {
    return factory.getBean(bean);
  }

  public void setBeanFactory(final BeanFactory factory) {
    this.factory = factory;

  }
}
