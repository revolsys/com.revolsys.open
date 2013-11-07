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

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.expression.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * The JavaBeanUtil is a utility class that provides methods to set/get
 * properties from an object with no checked exceptions thrown.
 * 
 * @author Paul Austin
 */
public final class JavaBeanUtil {
  public static final PropertyUtilsBean PROPERTY_UTILS_BEAN = new PropertyUtilsBean();

  public static final ConvertUtilsBean CONVERT_UTILS_BEAN = new ConvertUtilsBean();

  static final Logger LOG = LoggerFactory.getLogger(JavaBeanUtil.class);

  /**
   * Clone the value if it has a clone method.
   * 
   * @param value The value to clone.
   * @return The cloned value.
   */
  @SuppressWarnings("unchecked")
  public static <V> V clone(final V value) {
    if (value instanceof Map) {
      final Map<Object, Object> map = new LinkedHashMap<Object, Object>(
        (Map<Object, Object>)value);
      for (final Entry<Object, Object> entry : map.entrySet()) {
        final Object mapValue = entry.getValue();
        final Object clonedMapValue = clone(mapValue);
        entry.setValue(clonedMapValue);
      }
    } else if (value instanceof Cloneable) {
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

  @SuppressWarnings("rawtypes")
  public static Object convert(final Object value, final Class type) {
    final Converter converter = CONVERT_UTILS_BEAN.lookup(type);
    if (converter == null) {
      return value;
    } else {
      return converter.convert(type, value);
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V createInstance(final String className) {
    try {
      final Class<?> clazz = Class.forName(className);
      return (V)clazz.newInstance();
    } catch (final InstantiationException e) {
      return (V)ExceptionUtil.throwCauseException(e);
    } catch (final Throwable e) {
      return (V)ExceptionUtil.throwUncheckedException(e);
    }
  }

  public static boolean getBooleanValue(final Object object,
    final String attributeName) {
    if (object == null) {
      return false;
    } else {
      final Object value = Property.get(object, attributeName);
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

  public static Method getWriteMethod(final Class<?> beanClass,
    final String name) {
    final PropertyDescriptor descriptor = Property.descriptor(beanClass, name);
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

  public static boolean isAssignableFrom(final Collection<Class<?>> classes,
    final Class<?> objectClass) {
    for (final Class<?> allowedClass : classes) {
      if (allowedClass != null) {
        if (allowedClass.isAssignableFrom(objectClass)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isAssignableFrom(final Collection<Class<?>> classes,
    final Object object) {
    Class<?> objectClass;
    if (object == null) {
      return false;
    } else if (object instanceof Class<?>) {
      objectClass = (Class<?>)object;
    } else {
      objectClass = object.getClass();
    }
    return isAssignableFrom(classes, objectClass);
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

  public static <T> T method(final Method method, final Object object,
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
        throw new WrappedException(t);
      }
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T method(final Object object, final String methodName,
    final Object... args) {
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

  /**
   * Set the value of the named property on the object. Missing properties are
   * logged as debug statements.
   * 
   * @param object The object.
   * @param propertyName The name of the property.
   * @param value The property value.
   */
  public static boolean setProperty(final Object object, String propertyName,
    final Object value) {
    if (object == null || !StringUtils.hasText(propertyName)) {
      return false;
    } else {
      try {
        Object target = object;
        final Resolver resolver = PROPERTY_UTILS_BEAN.getResolver();
        while (resolver.hasNested(propertyName)) {
          try {
            target = PROPERTY_UTILS_BEAN.getProperty(target,
              resolver.next(propertyName));
            propertyName = resolver.remove(propertyName);
          } catch (final NoSuchMethodException e) {
            return false;
          }
        }

        final String propName = resolver.getProperty(propertyName);
        @SuppressWarnings("rawtypes")
        Class type = null;
        final int index = resolver.getIndex(propertyName);
        final String key = resolver.getKey(propertyName);

        if (target instanceof DynaBean) {
          final DynaClass dynaClass = ((DynaBean)target).getDynaClass();
          final DynaProperty dynaProperty = dynaClass.getDynaProperty(propName);
          if (dynaProperty == null) {
            return false;
          }
          type = dynaProperty.getType();
        } else if (target instanceof Map) {
          type = Object.class;
        } else if ((target != null) && (target.getClass().isArray())
          && (index >= 0)) {
          type = Array.get(target, index).getClass();
        } else {
          PropertyDescriptor descriptor = null;
          try {
            descriptor = PROPERTY_UTILS_BEAN.getPropertyDescriptor(target,
              propertyName);

            if (descriptor == null) {
              return false;
            }
          } catch (final NoSuchMethodException e) {
            return false;
          }
          if (descriptor instanceof MappedPropertyDescriptor) {
            if (((MappedPropertyDescriptor)descriptor).getMappedWriteMethod() == null) {
              return false;
            }
            type = ((MappedPropertyDescriptor)descriptor).getMappedPropertyType();
          } else if ((index >= 0)
            && (descriptor instanceof IndexedPropertyDescriptor)) {
            if (((IndexedPropertyDescriptor)descriptor).getIndexedWriteMethod() == null) {
              return false;
            }
            type = ((IndexedPropertyDescriptor)descriptor).getIndexedPropertyType();
          } else if (key != null) {
            if (descriptor.getReadMethod() == null) {
              return false;
            }
            type = (value == null) ? Object.class : value.getClass();
          } else {
            if (descriptor.getWriteMethod() == null) {
              final String setMethodName = "set"
                + CaseConverter.toUpperFirstChar(propertyName);
              Method setMethod = MethodUtils.getAccessibleMethod(
                target.getClass(), setMethodName, value.getClass());
              if (setMethod == null) {
                setMethod = MethodUtils.getAccessibleMethod(target.getClass(),
                  setMethodName, Double.TYPE);
              }
              if (setMethod != null) {
                method(setMethod, target, value);
                return true;
              }

              return false;
            }
            type = descriptor.getPropertyType();
          }

        }

        Object newValue = null;
        if ((type.isArray()) && (index < 0)) {
          if (value == null) {
            final String[] values = new String[1];
            values[0] = null;
            newValue = CONVERT_UTILS_BEAN.convert(values, type);
          } else if (value instanceof String) {
            newValue = CONVERT_UTILS_BEAN.convert(value, type);
          } else if (value instanceof String[]) {
            newValue = CONVERT_UTILS_BEAN.convert((String[])value, type);
          } else {
            newValue = convert(value, type);
          }
        } else if (type.isArray()) {
          if ((value instanceof String) || (value == null)) {
            newValue = CONVERT_UTILS_BEAN.convert((String)value,
              type.getComponentType());
          } else if (value instanceof String[]) {
            newValue = CONVERT_UTILS_BEAN.convert(((String[])value)[0],
              type.getComponentType());
          } else {
            newValue = convert(value, type.getComponentType());
          }
        } else if (value instanceof String) {
          newValue = CONVERT_UTILS_BEAN.convert((String)value, type);
        } else if (value instanceof String[]) {
          newValue = CONVERT_UTILS_BEAN.convert(((String[])value)[0], type);
        } else {
          newValue = convert(value, type);

        }

        try {
          PROPERTY_UTILS_BEAN.setProperty(target, propertyName, newValue);
          return true;
        } catch (final NoSuchMethodException e) {
          return false;
        }
      } catch (final IllegalAccessException e) {
        throw new RuntimeException(
          "Unable to access property: " + propertyName, e);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getCause();
        if (t instanceof RuntimeException) {
          throw (RuntimeException)t;
        } else if (t instanceof Error) {
          throw (Error)t;
        } else {
          throw new RuntimeException("Unable to set property: " + propertyName,
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
