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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl.Expression;
import org.apache.log4j.Logger;

import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class Page extends Component {
  private static final Logger LOG = Logger.getLogger(Page.class);

  private final List<Argument> arguments = new ArrayList<Argument>();

  private final Map<String, Argument> argumentsMap = new HashMap<String, Argument>();

  private final List<Attribute> attributes = new ArrayList<Attribute>();

  private final Map<String, Attribute> attributesMap = new HashMap<String, Attribute>();

  private Layout layout;

  private long menuId;

  private final Map<String, Menu> menus = new HashMap<String, Menu>();

  private final Map<String, Page> pages = new HashMap<String, Page>();

  private Page parent;

  private String path = "";

  private Map<String, Page> pathMap = new HashMap<String, Page>();

  private final Map<String, String> properties = new HashMap<String, String>();

  private boolean secure;

  private String title = "";

  private Expression titleExpression;

  public Page(
    final Page page) {
    super(page);
    menuId = page.menuId;
    path = page.path;
    title = page.title;
    properties.putAll(page.properties);
    arguments.addAll(page.arguments);
    argumentsMap.putAll(page.argumentsMap);
    attributes.addAll(page.attributes);
    attributesMap.putAll(page.attributesMap);
  }

  public Page(
    final String name,
    final String title,
    final String path,
    final boolean secure) {
    super(name);
    setTitle(title);
    setPath(path);
    this.secure = secure;
  }

  public void addArgument(
    final Argument argument) {
    if (!hasArgument(argument.getName())) {
      arguments.add(argument);
      argumentsMap.put(argument.getName(), argument);
    }
    if (argument.isInheritable()) {
      for (final Page page : this.pages.values()) {
        page.addArgument(argument);
      }
    }
  }

  public void addAttribute(
    final Attribute attribute) {
    if (!hasArgument(attribute.getName())) {
      attributes.add(attribute);
      attributesMap.put(attribute.getName(), attribute);
    }
    if (attribute.isInheritable()) {
      for (final Page page : pages.values()) {
        page.addAttribute(attribute);
      }
    }
  }

  public void addMenu(
    final Menu menu) {
    menus.put(menu.getName(), menu);
  }

  public void addPage(
    final Page page) {
    pages.put(page.getName(), page);
    pathMap.put(page.getPath(), page);
    page.setParent(this);
    for (final Entry<String, Page> entry : page.getPathMap().entrySet()) {
      final String path = page.getPath() + entry.getKey();
      final Page childPage = entry.getValue();
      pathMap.put(path, childPage);
    }

    for (final Argument argument : arguments) {
      if (argument.isInheritable()) {
        page.addArgument(argument);
      }
    }
    for (final Attribute attribute : attributes) {
      if (attribute.isInheritable()) {
        page.addAttribute(attribute);
      }
    }
  }

  public void addProperty(
    final String name,
    final String value) {
    properties.put(name, value);
  }

  @Override
  public Object clone() {
    return new Page(this);
  }

  @Override
  public boolean equals(
    final Object o) {
    if (o instanceof Page) {
      final Page p = (Page)o;
      if (super.equals(o)
        && p.menuId == menuId
        && p.path.equals(path)
        && (p.title == title || p.title != null && title != null
          && p.title.equals(title)) && p.properties.equals(properties)) {
        return true;
      }
    }
    return false;
  }

  public String getAbsolutePath() {
    if (parent != null) {
      return parent.getAbsolutePath() + path;
    } else {
      return WebUiContext.get().getConfig().getBasePath() + path;
    }
  }

  public List getArguments() {
    return arguments;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public String getFullPath() {
    if (secure) {
      return getAbsolutePath() + ".wps";
    } else {
      return getAbsolutePath();
    }
  }

  public String getFullUrl() {
    final Map<String, Object> parameters = Collections.emptyMap();
    return getFullUrl(parameters);
  }

  public String getFullUrl(
    final Map<String, ? extends Object> parameters) {
    final WebUiContext context = WebUiContext.get();
    final Map<String, Object> uriParameters = new HashMap<String, Object>(
      parameters);
    if (context != null) {
      final HttpServletRequest request = context.getRequest();
      if (request != null) {
        for (final Argument argument : arguments) {
          final String name = argument.getName();
          if (!uriParameters.containsKey(name)) {
            final String value = request.getParameter(name);
            if (value != null) {
              uriParameters.put(name, value);
            }
          }
        }
      }
    }
    return UrlUtil.getUrl(getFullPath(), uriParameters);
  }

  public Layout getLayout() {
    return layout;
  }

  public Menu getMenu(
    final String name) {
    return menus.get(name);
  }

  public long getMenuId() {
    return menuId;
  }

  public Page getPage(
    final String name) {
    if (name == null) {
      return null;
    }
    try {
      if (name.startsWith("/")) {
        return WebUiContext.get().getConfig().getPage(name);
      }
      Page page = pages.get(name);
      if (page == null) {
        final Page parent = getParent();
        if (parent != null) {
          page = parent.getPage(name);
        } else {
          page = WebUiContext.get().getConfig().getPage("/" + name);
        }
      }
      return page;
    } catch (final PageNotFoundException e) {
      return null;
    }
  }

  public Page getParent() {
    return parent;
  }

  public String getPath() {
    return path;
  }

  public Map<String, Page> getPathMap() {
    return pathMap;
  }

  public String getProperty(
    final String name) {
    return properties.get(name);
  }

  public String getTitle() {
    if (titleExpression != null) {
      final WebUiContext context = WebUiContext.get();
      return (String)context.evaluateExpression(titleExpression);
    } else {
      return title;
    }
  }

  public boolean hasArgument(
    final String name) {
    return argumentsMap.containsKey(name);
  }

  public boolean hasAttribute(
    final String name) {
    return attributesMap.containsKey(name);
  }

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return super.hashCode() + (path.hashCode() << 2);
  }

  public final boolean isSecure() {
    return secure;
  }

  public void setLayout(
    final Layout layout) {
    this.layout = layout;
    layout.setPage(this);
  }

  public void setMenuId(
    final long menuId) {
    this.menuId = menuId;
  }

  public void setParent(
    final Page parent) {
    this.parent = parent;
    if (parent.isSecure()) {
      secure = true;
    }
  }

  public void setPath(
    final String path) {
    if (path != null) {
      this.path = path;
    } else {
      this.path = "/" + CaseConverter.toLowerCamelCase(getName());
    }
  }

  public void setPathMap(
    final Map<String, Page> pathMap) {
    this.pathMap = pathMap;
  }

  public final void setSecure(
    final boolean secure) {
    this.secure = secure;
  }

  public void setTitle(
    final String title) {
    if (title != null) {
      this.title = title;
      try {
        titleExpression = JexlUtil.createExpression(title);
      } catch (final Exception e) {
        LOG.error(e.getMessage(), e);
      }
    } else {
      this.title = CaseConverter.toCapitalizedWords(getName());
    }
  }
}
