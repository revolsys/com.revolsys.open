package com.revolsys.ui.html.builder;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class HtmlUiBuilderFactory implements BeanFactoryAware {

  private static Map<BeanFactory, Map<Class<?>, HtmlUiBuilder<?>>> buildersByFactoryAndClass = new WeakHashMap<>();

  @SuppressWarnings("unchecked")
  public static <T extends HtmlUiBuilder> T get(final BeanFactory factory,
    final Class<?> objectClass) {
    HtmlUiBuilder<?> builder = null;
    if (objectClass != null) {
      Map<Class<?>, HtmlUiBuilder<?>> buildersByClass = buildersByFactoryAndClass.get(factory);
      if (buildersByClass == null) {
        buildersByClass = new WeakHashMap<>();
        buildersByFactoryAndClass.put(factory, buildersByClass);
      }
      builder = buildersByClass.get(factory);
      if (builder == null) {
        final Set<Class<?>> interfaces = new LinkedHashSet<>();
        builder = get(buildersByClass, interfaces, factory, objectClass);
        if (builder == null) {
          builder = get(buildersByClass, factory, objectClass, interfaces);
        }
      }
    }
    return (T)builder;
  }

  @SuppressWarnings("unchecked")
  public static <T extends HtmlUiBuilder> T get(final BeanFactory factory, final String typePath) {
    final String beanName = typePath + "-htmlbuilder";
    if (factory.containsBean(beanName)) {
      return (T)factory.getBean(beanName);
    } else {
      return null;
    }
  }

  private static HtmlUiBuilder<?> get(final Map<Class<?>, HtmlUiBuilder<?>> buildersByClass,
    final BeanFactory factory, final Class<?> objectClass, final Set<Class<?>> interfaces) {
    HtmlUiBuilder<?> builder = null;
    for (final Class<?> interfaceClass : interfaces) {
      builder = get(buildersByClass, interfaces, factory, interfaceClass);
      if (builder != null) {
        buildersByClass.put(objectClass, builder);
        return builder;
      }
    }
    return builder;
  }

  private static HtmlUiBuilder<?> get(final Map<Class<?>, HtmlUiBuilder<?>> buildersByClass,
    final Set<Class<?>> interfaces, final BeanFactory factory, final Class<?> objectClass) {
    HtmlUiBuilder<?> builder = null;
    if (objectClass != null) {
      builder = buildersByClass.get(objectClass);
      if (builder == null) {
        final String className = objectClass.getName();
        builder = get(factory, className);
        if (builder == null) {
          for (final Class<?> interfaceClass : objectClass.getInterfaces()) {
            interfaces.add(interfaceClass);
          }
          final Class<?> superClass = objectClass.getSuperclass();
          builder = get(buildersByClass, interfaces, factory, superClass);
        }
      }
    }
    if (builder != null) {
      buildersByClass.put(objectClass, builder);
    }
    return builder;
  }

  private BeanFactory beanFactory;

  @PreDestroy
  public void destory() {
    buildersByFactoryAndClass.remove(this.beanFactory);
  }

  public <T extends HtmlUiBuilder<?>> T get(final Class<?> objectClass) {
    return (T)get(this.beanFactory, objectClass);
  }

  public <T extends HtmlUiBuilder<?>> T get(final String objectClassName) {
    return (T)get(this.beanFactory, objectClassName);
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

}
