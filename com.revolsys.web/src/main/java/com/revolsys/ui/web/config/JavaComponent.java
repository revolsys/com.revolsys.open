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

import org.apache.commons.jexl.Expression;
import org.apache.log4j.Logger;

import com.revolsys.util.JexlUtil;

public class JavaComponent extends Component {
  private static final Logger log = Logger.getLogger(JavaComponent.class);

  private static final Class[] SERIALIZE_METHOD_ARGS = new Class[] {
    Writer.class
  };

  private static final Class[] SET_PROPERTY_ARGS = new Class[] {
    String.class, Object.class
  };

  private String className;

  private Map properties = new HashMap();

  private Class componentClass;

  private Method serializeMethod;

  private Method setPropertyMethod;

  public JavaComponent(final String area, final String name,
    final String className) {
    super(area, name);
    setClassName(className);
  }

  public JavaComponent(final JavaComponent component) {
    super(component);
    setClassName(component.className);
    this.properties.putAll(component.properties);
  }

  public Object clone() {
    return new JavaComponent(this);
  }

  public boolean equals(final Object o) {
    if (o instanceof JavaComponent) {
      JavaComponent c = (JavaComponent)o;
      if (c.className.equals(className) && super.equals(o)) {
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
  public int hashCode() {
    return className.hashCode();
  }

  public void includeComponent(final PageContext context)
    throws ServletException, IOException {
    Object instance;
    try {
      instance = componentClass.newInstance();
    } catch (Exception e) {
      throw new ServletException("Unable to create component instance", e);
    }
    try {
      Iterator propertyNames = properties.keySet().iterator();
      while (propertyNames.hasNext()) {
        String propertyName = (String)propertyNames.next();
        Object value = properties.get(propertyName);
        WebUiContext niceContext = WebUiContext.get();
        try {
          Expression expression = JexlUtil.createExpression(value.toString());
          if (expression != null) {
            value = niceContext.evaluateExpression(expression);
          }
        } catch (Exception e) {
          throw new ServletException(e.getMessage(), e);
        }

        setPropertyMethod.invoke(instance, new Object[] {
          propertyName, value
        });
      }
    } catch (IllegalAccessException e) {
      log.error("Unable to set component properties", e.getCause());
      throw new ServletException("Unable to set component properties", e);
    } catch (InvocationTargetException e) {
      log.error("Unable to set component properties", e.getCause());
      throw new ServletException("Unable to set component properties",
        e.getCause());
    }
    try {
      Writer out = context.getOut();
      serializeMethod.invoke(instance, new Object[] {
        out
      });
    } catch (IllegalAccessException e) {
      throw new ServletException("Unable to serialize component", e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
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
      componentClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    try {
      setPropertyMethod = componentClass.getMethod("setProperty",
        SET_PROPERTY_ARGS);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
        "Class "
          + className
          + " must have a method with the signature 'public void setProperty(String name, Object value)'");
    }
    try {
      serializeMethod = componentClass.getMethod("serialize",
        SERIALIZE_METHOD_ARGS);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
        "Class "
          + className
          + " must have a method with the signature 'public void serialize(Writer out) throws IOException'");
    }
  }

  public void setProperty(final String name, final String value) {
    properties.put(name, value);
  }

  public String toString() {
    StringBuffer s = new StringBuffer(className).append("(");
    for (Iterator props = properties.entrySet().iterator(); props.hasNext();) {
      Map.Entry prop = (Map.Entry)props.next();
      s.append(prop.getKey()).append("=").append(prop.getValue());
      if (props.hasNext()) {
        s.append(",");
      }
    }
    s.append(")");
    return s.toString();
  }
}
