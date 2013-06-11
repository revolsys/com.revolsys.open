/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObject;

/**
 * The JavaBeanUtil is a utility class that provides methods to set/get
 * properties from an object with no checked exceptions thrown.
 * 
 * @author Paul Austin
 */
public final class JavaBeanUtil {
  private static final Logger LOG = LoggerFactory.getLogger(JavaBeanUtil.class);

  /**
   * Clone the value if it has a clone method.
   * 
   * @param value The value to clone.
   * @return The cloned value.
   */
  @SuppressWarnings("unchecked")
  public static <V> V clone(final V value) {
    if (value instanceof Cloneable) {
      try {
        final Class<? extends Object> valueClass = value.getClass();
        final Method method = valueClass.getMethod("clone", new Class[0]);
        if (method != null) {
          return (V)method.invoke(value, new Object[0]);
        }
      } catch (final IllegalArgumentException e) {
        throw e;
      } catch (final InvocationTargetException e) {

        final Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          final RuntimeException re = (RuntimeException)cause;
          throw re;
        } else if (cause instanceof Error) {
          final Error ee = (Error)cause;
          throw ee;
        } else {
          throw new RuntimeException(cause.getMessage(), cause);
        }
      } catch (final RuntimeException e) {
        throw e;
      } catch (final Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }

    }
    return value;
  }

  public static boolean getBooleanValue(final Object object,
    final String attributeName) {
    if (object == null) {
      return false;
    } else {
      final Object value = getValue(object, attributeName);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("1") || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  public static String getFirstName(final String name) {
    if (StringUtils.hasText(name)) {
      final int index = name.indexOf(".");
      if (index == -1) {
        return name;
      } else {
        return name.substring(0, index);
      }
    }
    return name;
  }

  public static Method getMethod(final Class<?> clazz, final String name,
    final Class<?>... parameterTypes) {
    try {
      final Method method = clazz.getMethod(name, parameterTypes);
      return method;
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static List<Method> getMethods(final Class<?> clazz) {
    final Method[] methods = clazz.getMethods();
    Arrays.sort(methods, new Comparator<Method>() {
      @Override
      public int compare(final Method method1, final Method method2) {
        final String name1 = method1.getName()
          .replaceAll("^(set|get|is)", "")
          .toLowerCase();
        final String name2 = method2.getName()
          .replaceAll("^(set|get|is)", "")
          .toLowerCase();
        final int nameCompare = name1.compareTo(name2);
        return nameCompare;
      }
    });
    return Arrays.asList(methods);
  }

  /**
   * Get the value of the named property from the object. Any exceptions are
   * wrapped as runtime exceptions.
   * 
   * @param object The object.
   * @param propertyName The name of the property.
   * @return The property value.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getProperty(final Object object, final String propertyName) {
    try {
      return (T)PropertyUtils.getProperty(object, propertyName);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException("Unable to get property " + propertyName, e);
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getCause();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException("Unable to get property " + propertyName, t);
      }
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Property " + propertyName
        + " does not exist");
    }
  }

  public static PropertyDescriptor getPropertyDescriptor(
    final Class<?> beanClass, final String name) {
    try {
      final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
      final PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
      for (int i = 0; i < props.length; i++) {
        final PropertyDescriptor property = props[i];
        if (name.equals(property.getName())) {
          return property;
        }
      }
    } catch (final IntrospectionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getPropertyName(final String methodName) {
    String propertyName;
    if (methodName.startsWith("is")) {
      propertyName = methodName.substring(2, 3).toLowerCase()
        + methodName.substring(3);
    } else {
      propertyName = methodName.substring(3, 4).toLowerCase()
        + methodName.substring(4);
    }
    return propertyName;
  }

  public static Method getReadMethod(final Class<?> beanClass, final String name) {
    final PropertyDescriptor descriptor = getPropertyDescriptor(beanClass, name);
    if (descriptor != null) {
      return descriptor.getReadMethod();
    } else {
      return null;
    }
  }

  public static Method getReadMethod(final Object object, final String name) {
    return getReadMethod(object.getClass(), name);
  }

  /**
   * Get the value of the named property from the object. Any exceptions are
   * wrapped as runtime exceptions.
   * 
   * @param object The object.
   * @param propertyName The name of the property.
   * @return The property value.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getSimpleProperty(final Object object,
    final String propertyName) {
    if (object == null) {
      return null;
    } else {
      try {
        return (T)PropertyUtils.getSimpleProperty(object, propertyName);
      } catch (final IllegalAccessException e) {
        throw new RuntimeException("Unable to get property " + propertyName, e);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getCause();
        if (t instanceof RuntimeException) {
          throw (RuntimeException)t;
        } else if (t instanceof Error) {
          throw (Error)t;
        } else {
          throw new RuntimeException("Unable to get property " + propertyName,
            t);
        }
      } catch (final NoSuchMethodException e) {
        throw new IllegalArgumentException("Property " + propertyName
          + " does not exist");
      }
    }
  }

  public static String getSubName(final String name) {
    if (StringUtils.hasText(name)) {
      final int index = name.indexOf(".");
      if (index == -1) {
        return "";
      } else {
        return name.substring(index + 1);
      }
    }
    return name;
  }

  public static Class<?> getTypeParameterClass(final Method method,
    final Class<?> expectedRawClass) {
    final Type resultListReturnType = method.getGenericReturnType();
    if (resultListReturnType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType)resultListReturnType;
      final Type rawType = parameterizedType.getRawType();
      if (rawType == expectedRawClass) {
        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 1) {
          final Type resultType = typeArguments[0];
          if (resultType instanceof Class<?>) {
            final Class<?> resultClass = (Class<?>)resultType;
            return resultClass;
          } else {
            throw new IllegalArgumentException(method.getName()
              + " must return " + expectedRawClass.getName()
              + " with 1 generic type parameter that is a class");
          }
        }
      }
    }
    throw new IllegalArgumentException(method.getName() + " must return "
      + expectedRawClass.getName() + " with 1 generic class parameter");
  }

  public static Object getValue(final Object object, final String key) {
    if (object == null) {
      return null;
    } else {
      if (object instanceof DataObject) {
        final DataObject dataObject = (DataObject)object;
        return dataObject.getValueByPath(key);
      } else if (object instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, ?> map = (Map<String, ?>)object;
        return map.get(key);
      } else if (object instanceof Annotation) {
        final Annotation annotation = (Annotation)object;
        return AnnotationUtils.getValue(annotation, key);
      } else {
        final String firstName = getFirstName(key);
        final String subName = getSubName(key);
        final Object value = getProperty(object, firstName);
        if (value == null || !StringUtils.hasText(subName)) {
          return value;
        } else {
          return getValue(value, subName);
        }
      }
    }
  }

  public static Method getWriteMethod(final Class<?> beanClass,
    final String name) {
    final PropertyDescriptor descriptor = getPropertyDescriptor(beanClass, name);
    if (descriptor != null) {
      return descriptor.getWriteMethod();
    } else {
      return null;
    }
  }

  public static Method getWriteMethod(final Object object, final String name) {
    return getWriteMethod(object.getClass(), name);
  }

  public static <T> T invokeConstructor(
    final Constructor<? extends T> constructor, final Object... args) {
    try {
      final T object = constructor.newInstance(args);
      return object;
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T invokeMethod(final Method method, final Object object,
    final Object... args) {
    try {
      @SuppressWarnings("unchecked")
      final T result = (T)method.invoke(object, args);
      return result;
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T invokeMethod(final Object object,
    final String methodName, final Object... args) {
    try {
      return (T)MethodUtils.invokeMethod(object, methodName, args);
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isDefinedInClassLoader(final ClassLoader classLoader,
    final URL resourceUrl) {
    if (classLoader instanceof URLClassLoader) {
      final String resourceUrlString = resourceUrl.toString();
      final URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
      for (final URL url : urlClassLoader.getURLs()) {
        if (resourceUrlString.contains(url.toString())) {
          return true;
        }
      }
      return false;
    } else {
      return true;
    }
  }

  public static void setProperties(final Object object,
    final Map<String, ? extends Object> properties) {
    for (final Entry<String, ? extends Object> property : properties.entrySet()) {
      final String propertyName = property.getKey();
      final Object value = property.getValue();
      try {
        PropertyUtils.setProperty(object, propertyName, value);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getCause();
        LOG.debug("Unable to set property " + propertyName, t);
      } catch (final Throwable e) {
        LOG.debug("Unable to set property " + propertyName, e);
      }
    }
  }

  /**
   * Set the value of the named property on the object. Missing properties are
   * logged as debug statements.
   * 
   * @param object The object.
   * @param propertyName The name of the property.
   * @param value The property value.
   */
  public static void setProperty(final Object object,
    final String propertyName, final Object value) {
    if (object != null && StringUtils.hasText(propertyName)) {
      try {
        BeanUtils.setProperty(object, propertyName, value);
      } catch (final IllegalAccessException e) {
        throw new RuntimeException("Unable to set property " + propertyName, e);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getCause();
        if (t instanceof RuntimeException) {
          throw (RuntimeException)t;
        } else if (t instanceof Error) {
          throw (Error)t;
        } else {
          throw new RuntimeException("Unable to set property " + propertyName,
            e);
        }
      }
    }
  }

  /**
   * Construct a new JavaBeanUtil.
   */
  private JavaBeanUtil() {
  }
}
