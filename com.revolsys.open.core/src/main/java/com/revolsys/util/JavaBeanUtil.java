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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.DataObject;

/**
 * The JavaBeanUtil is a utility class that provides methods to set/get
 * properties from an object with no checked exceptions thrown.
 * 
 * @author Paul Austin
 */
public final class JavaBeanUtil {
  private static final Logger LOG = LoggerFactory.getLogger(JavaBeanUtil.class);

  public static Method getMethod(
    final Class<?> clazz,
    final String name,
    final Class<?>... parameterTypes) {
    try {
      final Method method = clazz.getMethod(name, parameterTypes);
      return method;
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get the value of the named propery from the object. Any exceptions are
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

  public static Class<?> getTypeParameterClass(
    final Method method,
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

  public static <T> T invokeConstructor(
    final Constructor<? extends T> constructor,
    final Object... args) {
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

  public static <T> T invokeMethod(
    final Method method,
    final Object object,
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

  public static void setProperties(
    final Object object,
    final Map<String, Object> properties) {
    for (final Entry<String, Object> property : properties.entrySet()) {
      final String propertyName = property.getKey();
      final Object value = property.getValue();
      try {
        PropertyUtils.setProperty(object, propertyName, value);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getCause();
        LOG.error("Unable to set property " + propertyName, t);
      } catch (final Throwable e) {
        LOG.error("Unable to set property " + propertyName, e);
      }
    }

  }

  /**
   * Set the value of the named propery on the object. Any exceptions are
   * wrapped as runtime exceptions.
   * 
   * @param object The object.
   * @param propertyName The name of the property.
   * @param value The property value.
   */
  public static void setProperty(
    final Object object,
    final String propertyName,
    final Object value) {
    try {
      PropertyUtils.setProperty(object, propertyName, value);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException("Unable to set property " + propertyName, e);
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getCause();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException("Unable to set property " + propertyName, e);
      }
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Property " + propertyName
        + " does not exist");
    }
  }

  /**
   * Construct a new JavaBeanUtil.
   */
  private JavaBeanUtil() {
  }

  public static Object getValue(final Object object, final String key) {
    if (object instanceof DataObject) {
      final DataObject dataObject = (DataObject)object;
      return dataObject.getValueByPath(key);
    } else if (object instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, ?> map = (Map<String, ?>)object;
      return map.get(key);
    } else {
      return getProperty(object, key);
    }
  }

}
