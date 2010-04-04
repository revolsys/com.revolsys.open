package com.revolsys.jump.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BeanUtil {
  private BeanUtil() {
  }

  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  public static Object getProperty(final Object object, final String name) {
    if (object != null) {
      Class<?> beanClass = object.getClass();
      try {
        Method method = getReadMethod(beanClass, name);
        if (method != null) {
          return method.invoke(object, EMPTY_OBJECT_ARRAY);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static Method getReadMethod(final Class<?> beanClass, final String name) {
    PropertyDescriptor descriptor = getPropertyDescriptor(beanClass, name);
    if (descriptor != null) {
      return descriptor.getReadMethod();
    } else {
      return null;
    }
  }

  public static Method getReadMethod(final Object object, final String name) {
    return getReadMethod(object.getClass(), name);
  }

  public static PropertyDescriptor getPropertyDescriptor(
    final Class<?> beanClass, final String name) {
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
      PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
      for (int i = 0; i < props.length; i++) {
        PropertyDescriptor property = props[i];
        if (name.equals(property.getName())) {
          return property;
        }
      }
    } catch (IntrospectionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void setProperty(final Object object, final String name,
    final Object value) {
    if (object != null) {
      Class<?> beanClass = object.getClass();
      try {
        Method method = getWriteMethod(beanClass, name);
        if (method != null) {
          method.invoke(object, new Object[] {
            value
          });
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  public static Method getWriteMethod(final Class<?> beanClass,
    final String name) {
    PropertyDescriptor descriptor = getPropertyDescriptor(beanClass, name);
    if (descriptor != null) {
      return descriptor.getWriteMethod();
    } else {
      return null;
    }
  }

  public static Method getWriteMethod(final Object object, final String name) {
    return getWriteMethod(object.getClass(), name);
  }

}
