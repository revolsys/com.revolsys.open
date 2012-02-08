package com.revolsys.ui.html.builder;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class HtmlUiBuilderFactory implements BeanFactoryAware {

  private BeanFactory beanFactory;

  private static Map<BeanFactory, Map<Class<?>, HtmlUiBuilder<?>>> buildersByFactoryAndClass = new WeakHashMap<BeanFactory, Map<Class<?>, HtmlUiBuilder<?>>>();

  @SuppressWarnings("unchecked")
  public static <T extends HtmlUiBuilder> T get(
    final BeanFactory factory,
    final Class<?> objectClass) {
    HtmlUiBuilder<?> builder = null;
    if (objectClass != null) {
      Map<Class<?>, HtmlUiBuilder<?>> buildersByClass = buildersByFactoryAndClass.get(factory);
      if (buildersByClass == null) {
        buildersByClass = new WeakHashMap<Class<?>, HtmlUiBuilder<?>>();
        buildersByFactoryAndClass.put(factory, buildersByClass);
      }
      builder = buildersByClass.get(factory);
      if (builder == null) {
        Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        builder = get(buildersByClass, interfaces, factory, objectClass);
        if (builder == null) {
          builder = get(buildersByClass, factory, objectClass, interfaces);
        }
      }
    }
    return (T)builder;
  }

  private static HtmlUiBuilder<?> get(
    Map<Class<?>, HtmlUiBuilder<?>> buildersByClass,
    final BeanFactory factory,
    final Class<?> objectClass,
    Set<Class<?>> interfaces) {
    HtmlUiBuilder<?> builder = null;
    for (Class<?> interfaceClass : interfaces) {
      builder = get(buildersByClass, interfaces, factory, interfaceClass);
      if (builder != null) {
        buildersByClass.put(objectClass, builder);
        return builder;
      }
    }
    return builder;
  }

  private static HtmlUiBuilder<?> get(
    Map<Class<?>, HtmlUiBuilder<?>> buildersByClass,
    Set<Class<?>> interfaces,
    final BeanFactory factory,
    final Class<?> objectClass) {
    HtmlUiBuilder<?> builder = null;
    if (objectClass != null) {
      builder = buildersByClass.get(objectClass);
      if (builder == null) {
        String className = objectClass.getName();
        builder = (HtmlUiBuilder<?>)get(factory, className);
        if (builder == null) {
          for (Class<?> interfaceClass : objectClass.getInterfaces()) {
            interfaces.add(interfaceClass);
          }
          Class<?> superClass = objectClass.getSuperclass();
          builder = get(buildersByClass, interfaces, factory, superClass);
        }
      }
    }
    if (builder != null) {
      buildersByClass.put(objectClass, builder);
    }
    return builder;
  }

  @SuppressWarnings("unchecked")
  public static <T extends HtmlUiBuilder> T get(
    final BeanFactory factory,
    final String typeName) {
    String beanName = typeName + "-htmlbuilder";
    if (factory.containsBean(beanName)) {
      return (T)factory.getBean(beanName);
    } else {
      return null;
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
