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

public class Attribute {
  private static final Logger log = LoggerFactory.getLogger(Attribute.class);

  private final Config config;

  private final boolean inheritable;

  private AttributeLoader loader;

  private final Class loaderClass;

  private final String name;

  /** The initialization parameters for the argument. */
  private final HashMap parameters = new HashMap();

  private Object value;

  public Attribute(final Config config, final String name, final Class type, final String value,
    final boolean inheritable, final Class loaderClass) {
    this.config = config;
    this.name = name;
    this.inheritable = inheritable;
    if (type != null) {
      try {
        final Constructor constructor = type.getConstructor(new Class[] {
          String.class
        });
        this.value = constructor.newInstance(new Object[] {
          value
        });
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

      } catch (final NoSuchMethodException e) {
        throw new IllegalArgumentException(
          type.getName() + " must have a constructor that takes a java.lang.String as an argument");
      }
    }
    this.loaderClass = loaderClass;
  }

  /**
   * Add a new parameter to the action.
   *
   * @param parameter The parameter.
   */
  public void addParameter(final Parameter parameter) {
    this.parameters.put(parameter.getName(), parameter.getValue());
  }

  /**
   * Add a new parameter to the action.
   *
   * @param name The parameter name.
   * @param value The parameter value.
   */
  public void addParameter(final String name, final String value) {
    this.parameters.put(name, value);
  }

  public Config getConfig() {
    return this.config;
  }

  public AttributeLoader getLoader() {
    return this.loader;
  }

  public String getName() {
    return this.name;
  }

  /**
   * Get the parameter value.
   *
   * @param name The parameter name.
   * @return The parameter value.
   */
  public Object getParameter(final String name) {
    return this.parameters.get(name);
  }

  public Object getValue() {
    return this.value;
  }

  public void init() {
    if (this.loaderClass != null) {
      try {
        this.loader = (AttributeLoader)this.loaderClass.newInstance();
        this.loader.init(this);
      } catch (final Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  public boolean isInheritable() {
    return this.inheritable;
  }

}
