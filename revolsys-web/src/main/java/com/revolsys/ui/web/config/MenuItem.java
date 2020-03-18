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

import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class MenuItem implements Cloneable, Comparable {
  private static final Logger log = LoggerFactory.getLogger(MenuItem.class);

  private String anchor;

  private JexlExpression condition;

  private String name;

  private final Map parameters = new HashMap();

  private final Map properties = new HashMap();

  private Map staticParameters = new HashMap();

  private String title;

  private JexlExpression titleExpression;

  private String uri;

  private JexlExpression uriExpression;

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

  public MenuItem(final String name, final String title, final String uri, final String anchor,
    final String condition) {
    this(name, title, uri);
    this.anchor = anchor;
    setCondition(condition);
  }

  public void addParameter(final Parameter parameter) throws Exception {
    addParameter(parameter.getName(), parameter.getValue());
  }

  public void addParameter(final String name, final Object value) throws Exception {
    if (value instanceof String) {
      final String stringValue = (String)value;
      final JexlExpression expression = JexlUtil.newExpression(stringValue);
      if (expression != null) {
        this.parameters.put(name, expression);
      } else {
        this.staticParameters.put(name, value);
      }
    } else {
      this.staticParameters.put(name, value);
    }
  }

  public void addProperty(final String name, final String value) {
    this.properties.put(name, value);
  }

  public void addProperty(final WebProperty webProperty) {
    addProperty(webProperty.getName(), webProperty.getValue());
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public int compareTo(final Object o) {
    if (o instanceof MenuItem) {
      final MenuItem menuItem = (MenuItem)o;
      return this.title.compareTo(menuItem.getTitle());
    }
    return 1;
  }

  /**
   * @return Returns the anchor.
   */
  public String getAnchor() {
    return this.anchor;
  }

  /**
   * @return Returns the condition.
   */
  public JexlExpression getCondition() {
    return this.condition;
  }

  public String getName() {
    return this.name;
  }

  public final Map getParameters() {
    return this.parameters;
  }

  public Map getProperties() {
    return this.properties;
  }

  public String getProperty(final String name) {
    return (String)this.properties.get(name);
  }

  public final Map getStaticParameters() {
    return this.staticParameters;
  }

  public String getTitle() {
    final WebUiContext context = WebUiContext.get();
    if (this.titleExpression != null) {
      return (String)context.evaluateExpression(this.titleExpression);
    } else {
      return this.title;
    }
  }

  public String getUri() {
    final WebUiContext context = WebUiContext.get();
    String uri = this.uri;
    if (uri != null) {
      // If this is the first call to getUri update the uri with any static
      // parameters
      if (this.staticParameters != null) {
        uri = UrlUtil.getUrl(uri, this.staticParameters);
        this.uri = uri;
        this.staticParameters = null;
      }
    } else if (this.uriExpression != null) {
      uri = (String)context.evaluateExpression(this.uriExpression);
      if (this.staticParameters != null) {
        uri = UrlUtil.getUrl(uri, this.staticParameters);
      }
    }
    if (uri != null && this.parameters.size() > 0) {
      final Map qsParams = new HashMap();
      for (final Iterator params = this.parameters.entrySet().iterator(); params.hasNext();) {
        final Map.Entry param = (Map.Entry)params.next();
        final Object key = param.getKey();
        final Object value = context.evaluateExpression((JexlExpression)param.getValue());
        qsParams.put(key, value);
      }
      if (this.anchor == null) {
        return UrlUtil.getUrl(uri, qsParams);
      } else {
        return UrlUtil.getUrl(uri, qsParams) + "#" + this.anchor;
      }
    } else {
      if (this.anchor == null) {
        return uri;
      } else {
        return uri + "#" + this.anchor;
      }
    }
  }

  public boolean isVisible() {
    final WebUiContext context = WebUiContext.get();
    if (this.condition != null) {
      return ((Boolean)context.evaluateExpression(this.condition)).booleanValue();
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
        this.condition = JexlUtil.newExpression(condition);
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
      JexlExpression titleExpression = null;
      try {
        titleExpression = JexlUtil.newExpression(title);
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
      JexlExpression uriExpression = null;
      try {
        uriExpression = JexlUtil.newExpression(uri.replaceAll(" ", "%20"));
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
