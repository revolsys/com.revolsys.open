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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jexl.Expression;
import org.apache.log4j.Logger;

import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class MenuItem implements Cloneable, Comparable {
  private static final Logger log = Logger.getLogger(MenuItem.class);

  private String title;

  private String name;

  private String anchor;

  private String uri;

  private Expression uriExpression;

  private final Map parameters = new HashMap();

  private final Map properties = new HashMap();

  private Map staticParameters = new HashMap();

  private Expression condition;

  private Expression titleExpression;

  public MenuItem() {
  }

  public MenuItem(final String title, final String uri) {
    this(title, title, uri);
  }

  public MenuItem(final String name, final String title, final String uri) {
    this.name = name;
    setTitle(title);
    setUri(uri);
  }

  public MenuItem(final String name, final String title, final String uri,
    final String anchor, final String condition) {
    this(name, title, uri);
    this.anchor = anchor;
    setCondition(condition);
  }

  public void addParameter(final Parameter parameter) throws Exception {
    addParameter(parameter.getName(), parameter.getValue());
  }

  public void addParameter(final String name, final Object value)
    throws Exception {
    if (value instanceof String) {
      final String stringValue = (String)value;
      final Expression expression = JexlUtil.createExpression(stringValue);
      if (expression != null) {
        parameters.put(name, expression);
      } else {
        staticParameters.put(name, value);
      }
    } else {
      staticParameters.put(name, value);
    }
  }

  public void addProperty(final Property property) {
    addProperty(property.getName(), property.getValue());
  }

  public void addProperty(final String name, final String value) {
    properties.put(name, value);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public int compareTo(final Object o) {
    if (o instanceof MenuItem) {
      final MenuItem menuItem = (MenuItem)o;
      return title.compareTo(menuItem.getTitle());
    }
    return 1;
  }

  /**
   * @return Returns the anchor.
   */
  public String getAnchor() {
    return anchor;
  }

  /**
   * @return Returns the condition.
   */
  public Expression getCondition() {
    return condition;
  }

  public String getName() {
    return name;
  }

  public final Map getParameters() {
    return parameters;
  }

  public Map getProperties() {
    return properties;
  }

  public String getProperty(final String name) {
    return (String)properties.get(name);
  }

  public final Map getStaticParameters() {
    return staticParameters;
  }

  public String getTitle() {
    final WebUiContext context = WebUiContext.get();
    if (titleExpression != null) {
      return (String)context.evaluateExpression(titleExpression);
    } else {
      return title;
    }
  }

  public String getUri() {
    final WebUiContext context = WebUiContext.get();
    String uri = this.uri;
    if (uri != null) {
      // If this is the first call to getUri update the uri with any static
      // parameters
      if (staticParameters != null) {
        uri = UrlUtil.getUrl(uri, staticParameters);
        this.uri = uri;
        staticParameters = null;
      }
    } else if (uriExpression != null) {
      uri = (String)context.evaluateExpression(uriExpression);
      if (staticParameters != null) {
        uri = UrlUtil.getUrl(uri, staticParameters);
      }
    }
    if (uri != null && parameters.size() > 0) {
      final Map qsParams = new HashMap();
      for (final Iterator params = parameters.entrySet().iterator(); params.hasNext();) {
        final Map.Entry param = (Map.Entry)params.next();
        final Object key = param.getKey();
        final Object value = context.evaluateExpression((Expression)param.getValue());
        qsParams.put(key, value);
      }
      if (anchor == null) {
        return UrlUtil.getUrl(uri, qsParams);
      } else {
        return UrlUtil.getUrl(uri, qsParams) + "#" + anchor;
      }
    } else {
      if (anchor == null) {
        return uri;
      } else {
        return uri + "#" + anchor;
      }
    }
  }

  public boolean isVisible() {
    final WebUiContext context = WebUiContext.get();
    if (condition != null) {
      return ((Boolean)context.evaluateExpression(condition)).booleanValue();
    } else {
      return true;
    }
  }

  /**
   * @param anchor The anchor to set.
   */
  public void setAnchor(final String anchor) {
    this.anchor = anchor;
  }

  /**
   * @param condition The condition to set.
   */
  public void setCondition(final String condition) {
    if (condition != null) {
      try {
        this.condition = JexlUtil.createExpression(condition);
      } catch (final Throwable e) {
        log.error("Invalid Condition", e);
      }
    } else {
      this.condition = null;
    }
  }

  /**
   * @param refname
   */
  public void setName(final String name) {
    this.name = name;
  }

  public void setTitle(final String title) {
    if (title != null) {
      Expression titleExpression = null;
      try {
        titleExpression = JexlUtil.createExpression(title);
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
      }
      if (titleExpression == null) {
        this.title = title;
        this.titleExpression = null;
      } else {
        this.title = null;
        this.titleExpression = titleExpression;
      }
    } else {
      this.title = null;
      this.titleExpression = null;
    }
  }

  public void setUri(final String uri) {
    if (uri != null) {
      Expression uriExpression = null;
      try {
        uriExpression = JexlUtil.createExpression(uri.replaceAll(" ", "%20"));
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
      }
      if (uriExpression == null) {
        this.uri = uri.replaceAll(" ", "%20");
        this.uriExpression = null;
      } else {
        this.uri = null;
        this.uriExpression = uriExpression;
      }
    } else {
      this.uri = null;
      this.uriExpression = null;
    }
  }
}
