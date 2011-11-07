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

import org.apache.commons.beanutils.PropertyUtils;

/**
 * The JavaBeanUtil is a utility class that provides methods to set/get
 * properties from an object with no checked exceptions thrown.
 * 
 * @author Paul Austin
 */
public final class JavaBeanUtil {
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

  public static <T> T invokeConstructor(Constructor<? extends T> constructor,
    Object... args) {
    try {
      T object = constructor.newInstance(args);
      return object;
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static <T> T invokeMethod(Method method, Object object,
    Object... args) {
    try {
      @SuppressWarnings("unchecked")
      T result = (T)method.invoke(object, args);
      return result;
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Method getMethod(Class<?> clazz, String name,
    Class<?>... parameterTypes) {
    try {
      Method method = clazz.getMethod(name, parameterTypes);
      return method;
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Construct a new JavaBeanUtil.
   */
  private JavaBeanUtil() {
  }

}
