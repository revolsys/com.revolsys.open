package com.revolsys.ui.html.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class HtmlUiBuilderFactory implements BeanFactoryAware {

  private BeanFactory beanFactory;

  @SuppressWarnings("unchecked")
  public static <T extends HtmlUiBuilder> T get(final BeanFactory factory,
    final Class<?> objectClass) {
    try {
      if (objectClass == null) {
        return null;
      } else {
        String className = objectClass.getName();
        return (T)get(factory, className);
      }
    } catch (IllegalArgumentException e) {
      Class<?> superclass = objectClass.getSuperclass();
      return (T)get(factory, superclass);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends HtmlUiBuilder> T get(final BeanFactory factory,
    final String typeName) {
    String beanName = typeName + "-htmlbuilder";
    if (factory.containsBean(beanName)) {
      return (T)factory.getBean(beanName);
    } else {
      throw new IllegalArgumentException("No HTML UI Builder defined for: "
        + typeName);
    }

  }

  public void setBeanFactory(final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public <T extends HtmlUiBuilder<?>> T get(final Class<?> objectClass) {
    return (T)get(beanFactory, objectClass);
  }

  public <T extends HtmlUiBuilder<?>> T get(final String objectClassName) {
    return (T)get(beanFactory, objectClassName);
  }

}
