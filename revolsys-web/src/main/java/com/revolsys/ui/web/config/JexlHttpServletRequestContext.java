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

import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl3.JexlContext;
import org.springframework.web.util.UrlPathHelper;

public class JexlHttpServletRequestContext implements JexlContext {
  private final HttpServletRequest request;

  private final ServletContext servletContext;

  private final UrlPathHelper urlPathHelper = new UrlPathHelper();

  public JexlHttpServletRequestContext(final HttpServletRequest request) {
    this.request = request;

    this.servletContext = WebUiContext.getServletContext();
  }

  @Override
  public Object get(final String name) {
    return getVars().get(name);
  }

  public Map getVars() {
    return new AbstractMap() {
      @Override
      public Set entrySet() {
        final Map map = new HashMap();
        map.putAll(JexlHttpServletRequestContext.this.request.getParameterMap());
        for (final Enumeration names = JexlHttpServletRequestContext.this.request
          .getAttributeNames(); names.hasMoreElements();) {
          final String name = (String)names.nextElement();
          map.put(name, JexlHttpServletRequestContext.this.request.getAttribute(name));
        }
        if (JexlHttpServletRequestContext.this.servletContext != null) {
          for (final Enumeration names = JexlHttpServletRequestContext.this.servletContext
            .getAttributeNames(); names.hasMoreElements();) {
            final String name = (String)names.nextElement();
            map.put(name, JexlHttpServletRequestContext.this.servletContext.getAttribute(name));
          }
        }
        return map.entrySet();
      }

      @Override
      public Object get(final Object key) {
        if (key.equals("request")) {
          return JexlHttpServletRequestContext.this.request;
        } else if (key.equals("requestURI")) {
          return JexlHttpServletRequestContext.this.urlPathHelper
            .getOriginatingRequestUri(JexlHttpServletRequestContext.this.request);
        }
        final String keyString = key.toString();
        Object value = null;
        if (JexlHttpServletRequestContext.this.servletContext != null) {
          value = JexlHttpServletRequestContext.this.servletContext.getAttribute(keyString);
        }
        if (value == null) {
          value = JexlHttpServletRequestContext.this.request.getAttribute(keyString);
          if (value == null) {
            value = JexlHttpServletRequestContext.this.request.getParameter(keyString);
          }
        }
        if (value == null) {
          return "";
        } else {
          return value;
        }
      }
    };
  }

  @Override
  public boolean has(final String name) {
    return getVars().containsKey(name);
  }

  @Override
  public void set(final String name, final Object value) {
  }
}
