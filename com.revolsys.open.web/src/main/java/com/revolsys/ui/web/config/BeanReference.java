package com.revolsys.ui.web.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class BeanReference implements BeanFactoryAware {
  private String bean;

  private BeanFactory factory;

  private String name;

  /**
   * @return Returns the bean.
   */
  public String getBean() {
    return this.bean;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return this.name;
  }

  public Object getReferencedBean() {
    return this.factory.getBean(this.bean);
  }

  /**
   * @param bean The bean to set.
   */
  public void setBean(final String bean) {
    this.bean = bean;
  }

  @Override
  public void setBeanFactory(final BeanFactory factory) {
    this.factory = factory;

  }

  /**
   * @param name The name to set.
   */
  public void setName(final String name) {
    this.name = name;
  }
}
