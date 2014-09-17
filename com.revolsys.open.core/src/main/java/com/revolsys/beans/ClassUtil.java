package com.revolsys.beans;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassUtil {
  public static void addSuperClasses(
    final Set<Class<? extends Object>> classes,
    final Class<? extends Object> clazz) {
    if (clazz != null) {
      classes.add(clazz);
      final Class<?> superclass = clazz.getSuperclass();
      addSuperClasses(classes, superclass);
    }
  }

  public static void addSuperClassesAndInterfaces(
    final Set<Class<? extends Object>> classes,
    final Class<? extends Object> clazz) {
    if (clazz != null) {
      classes.add(clazz);
      final Class<?>[] interfaceClasses = clazz.getInterfaces();
      for (final Class<?> interfaceClass : interfaceClasses) {
        addSuperClassesAndInterfaces(classes, interfaceClass);
      }
      final Class<?> superClass = clazz.getSuperclass();
      addSuperClassesAndInterfaces(classes, superClass);
    }
  }

  public static String getClassName(final Class<?> clazz) {
    final String name = clazz.getName();
    final int index = name.lastIndexOf('.');
    if (index == -1) {
      return name;
    } else {
      return name.substring(index + 1);
    }
  }

  public static Set<Class<? extends Object>> getSuperClasses(
    final Class<? extends Object> clazz) {
    final Set<Class<? extends Object>> classes = new LinkedHashSet<Class<? extends Object>>();
    addSuperClasses(classes, clazz);
    return Collections.unmodifiableSet(classes);
  }

  public static Set<Class<? extends Object>> getSuperClassesAndInterfaces(
    final Class<? extends Object> clazz) {
    final Set<Class<? extends Object>> classes = new LinkedHashSet<Class<? extends Object>>();
    addSuperClassesAndInterfaces(classes, clazz);
    return Collections.unmodifiableSet(classes);
  }
}
