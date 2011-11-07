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

import org.apache.log4j.Logger;

public class Argument {
  private static final Logger log = Logger.getLogger(Argument.class);
  private String name;

  private String defaultValue;

  private Class type;

  private boolean required;

  private Constructor constructor;

  private boolean inheritable;

  /** The initialization parameters for the argument. */
  private HashMap parameters = new HashMap();

  public Argument(final String name, final Class type,
    final String defaultValue, final boolean required,
    final boolean inheritable) {
    this.name = name;
    this.type = type;
    this.defaultValue = defaultValue;
    this.required = required;
    this.inheritable = inheritable;
    if (type != null) {
      try {
        constructor = type.getConstructor(new Class[] {
          String.class
        });
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException(
          type.getName()
            + " must have a constructor that takes a java.lang.String as an argument");
      }
    }
  }


  public String getDefault() {
    return defaultValue;
  }

  public String getName() {
    return name;
  }

  public Class getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isInheritable() {
    return inheritable;
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
        defaultValue
      };
    } else {
      args = new Object[] {
        value
      };
    }
    try {
      return constructor.newInstance(args);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
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
