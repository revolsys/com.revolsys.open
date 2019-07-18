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

public class Parameter {
  private final String name;

  private final Class type;

  private Object value;

  public Parameter(final String name, final String value) {
    this(name, value, String.class);
  }

  public Parameter(final String name, final String value, final Class type) {
    this.name = name;
    this.type = type;
    if (type == null || type == String.class) {
      this.value = value;
    } else {

      try {
        final Constructor constructor = type.getConstructor(new Class[] {
          String.class
        });
        this.value = constructor.newInstance(new Object[] {
          value
        });
      } catch (final NoSuchMethodException e) {
        throw new IllegalArgumentException(
          type.getName() + " must have a constructor that takes a java.lang.String as an argument");
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

  public String getName() {
    return this.name;
  }

  public Class getType() {
    return this.type;
  }

  public Object getValue() {
    return this.value;
  }
}
