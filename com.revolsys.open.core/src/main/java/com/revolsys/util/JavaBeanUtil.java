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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JavaBeanUtil is a utility class that provides methods to set/get
 * properties from an object with no checked exceptions thrown.
 * 
 * @author Paul Austin
 */
public final class JavaBeanUtil {
  private static final Logger LOG = LoggerFactory.getLogger(JavaBeanUtil.class);

  public static Method getMethod(final Class<?> clazz, final String name,
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

  public static void setProperties(final Object object,
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
  public static void setProperty(final Object object,
    final String propertyName, final Object value) {
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

}
