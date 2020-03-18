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
package com.revolsys.ui.web.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Argument {
  private static final Logger log = LoggerFactory.getLogger(Argument.class);

  private Constructor constructor;

  private final String defaultValue;

  private final boolean inheritable;

  private final String name;

  /** The initialization parameters for the argument. */
  private final HashMap parameters = new HashMap();

  private final boolean required;

  private final Class type;

  public Argument(final String name, final Class type, final String defaultValue,
    final boolean required, final boolean inheritable) {
    this.name = name;
    this.type = type;
    this.defaultValue = defaultValue;
    this.required = required;
    this.inheritable = inheritable;
    if (type != null) {
      try {
        this.constructor = type.getConstructor(new Class[] {
          String.class
        });
      } catch (final NoSuchMethodException e) {
        throw new IllegalArgumentException(
          type.getName() + " must have a constructor that takes a java.lang.String as an argument");
      }
    }
  }

  public String getDefault() {
    return this.defaultValue;
  }

  public String getName() {
    return this.name;
  }

  public Class getType() {
    return this.type;
  }

  public boolean isInheritable() {
    return this.inheritable;
  }

  public boolean isRequired() {
    return this.required;
  }

  /**
   * Convert the string value into an object of the specified type.
   *
   * @param value
   * @return
   */
  public Object valueOf(final String value) {
    Object[] args;
    if (value == null) {
      args = new Object[] {
        this.defaultValue
      };
    } else {
      args = new Object[] {
        value
      };
    }
    try {
      return this.constructor.newInstance(args);
    } catch (final InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    }

  }
}
