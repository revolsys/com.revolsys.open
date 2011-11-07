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

import org.apache.commons.jexl.JexlContext;
import org.springframework.web.util.UrlPathHelper;

public class JexlHttpServletRequestContext implements JexlContext {
  private HttpServletRequest request;

  private ServletContext servletContext;

  private UrlPathHelper urlPathHelper = new UrlPathHelper();

  public JexlHttpServletRequestContext(final HttpServletRequest request) {
    this.request = request;

    this.servletContext = WebUiContext.getServletContext();
  }

  public void setVars(final Map arg0) {
  }

  public Map getVars() {
    return new AbstractMap() {
      public Object get(final Object key) {
        if (key.equals("request")) {
          return request;
        } else if (key.equals("requestURI")) {
          return urlPathHelper.getOriginatingRequestUri(request);
        }
        String keyString = key.toString();
        Object value = null;
        if (servletContext != null) {
          value = servletContext.getAttribute(keyString);
        }
        if (value == null) {
          value = request.getAttribute(keyString);
          if (value == null) {
            value = request.getParameter(keyString);
          }
        }
        if (value == null) {
          return "";
        } else {
          return value;
        }
      }

      public Set entrySet() {
        Map map = new HashMap();
        map.putAll(request.getParameterMap());
        for (Enumeration names = request.getAttributeNames(); names.hasMoreElements();) {
          String name = (String)names.nextElement();
          map.put(name, request.getAttribute(name));
        }
        if (servletContext != null) {
          for (Enumeration names = servletContext.getAttributeNames(); names.hasMoreElements();) {
            String name = (String)names.nextElement();
            map.put(name, servletContext.getAttribute(name));
          }
        }
        return map.entrySet();
      }
    };
  }

}
