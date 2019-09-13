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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.util.JexlUtil;

public class JavaComponent extends Component {
  private static final Logger log = LoggerFactory.getLogger(JavaComponent.class);

  private static final Class[] SERIALIZE_METHOD_ARGS = new Class[] {
    Writer.class
  };

  private static final Class[] SET_PROPERTY_ARGS = new Class[] {
    String.class, Object.class
  };

  private String className;

  private Class componentClass;

  private final Map properties = new HashMap();

  private Method serializeMethod;

  private Method setPropertyMethod;

  public JavaComponent(final JavaComponent component) {
    super(component);
    setClassName(component.className);
    this.properties.putAll(component.properties);
  }

  public JavaComponent(final String area, final String name, final String className) {
    super(area, name);
    setClassName(className);
  }

  @Override
  public Object clone() {
    return new JavaComponent(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof JavaComponent) {
      final JavaComponent c = (JavaComponent)o;
      if (c.className.equals(this.className) && super.equals(o)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate the hash code for the object.
   *
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return this.className.hashCode();
  }

  @Override
  public void includeComponent(final PageContext context) throws ServletException, IOException {
    Object instance;
    try {
      instance = this.componentClass.newInstance();
    } catch (final Exception e) {
      throw new ServletException("Unable to create component instance", e);
    }
    try {
      final Iterator propertyNames = this.properties.keySet().iterator();
      while (propertyNames.hasNext()) {
        final String propertyName = (String)propertyNames.next();
        Object value = this.properties.get(propertyName);
        final WebUiContext niceContext = WebUiContext.get();
        try {
          final JexlExpression expression = JexlUtil.newExpression(value.toString());
          if (expression != null) {
            value = niceContext.evaluateExpression(expression);
          }
        } catch (final Exception e) {
          throw new ServletException(e.getMessage(), e);
        }

        this.setPropertyMethod.invoke(instance, new Object[] {
          propertyName, value
        });
      }
    } catch (final IllegalAccessException e) {
      log.error("Unable to set component properties", e.getCause());
      throw new ServletException("Unable to set component properties", e);
    } catch (final InvocationTargetException e) {
      log.error("Unable to set component properties", e.getCause());
      throw new ServletException("Unable to set component properties", e.getCause());
    }
    try {
      final Writer out = context.getOut();
      this.serializeMethod.invoke(instance, new Object[] {
        out
      });
    } catch (final IllegalAccessException e) {
      throw new ServletException("Unable to serialize component", e);
    } catch (final InvocationTargetException e) {
      final Throwable cause = e.getCause();
      log.error(cause.getMessage(), cause);
      if (cause instanceof IOException) {
        throw (IOException)cause;
      }
      throw new ServletException("Unable to serialize component", cause);
    }
  }

  private void setClassName(final String className) {
    this.className = className;
    try {
      this.componentClass = Class.forName(className);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    try {
      this.setPropertyMethod = this.componentClass.getMethod("setProperty", SET_PROPERTY_ARGS);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Class " + className
        + " must have a method with the signature 'public void setProperty(String name, Object value)'");
    }
    try {
      this.serializeMethod = this.componentClass.getMethod("serialize", SERIALIZE_METHOD_ARGS);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Class " + className
        + " must have a method with the signature 'public void serialize(Writer out) throws IOException'");
    }
  }

  public void setProperty(final String name, final String value) {
    this.properties.put(name, value);
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder(this.className).append("(");
    for (final Iterator props = this.properties.entrySet().iterator(); props.hasNext();) {
      final Map.Entry prop = (Map.Entry)props.next();
      s.append(prop.getKey()).append("=").append(prop.getValue());
      if (props.hasNext()) {
        s.append(",");
      }
    }
    s.append(")");
    return s.toString();
  }
}
