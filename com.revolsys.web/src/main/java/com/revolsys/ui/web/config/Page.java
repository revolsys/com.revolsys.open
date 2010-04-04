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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl.Expression;
import org.apache.log4j.Logger;

import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class Page extends Component {
  private static final Logger log = Logger.getLogger(Page.class);

  private List arguments = new ArrayList();

  private long menuId;

  private String path = "";

  private String title = "";

  private HashMap properties = new HashMap();

  private HashMap pages = new HashMap();

  private HashMap menus = new HashMap();

  private HashMap pathMap = new HashMap();

  private Layout layout;

  private Page parent;

  private Map argumentsMap = new HashMap();

  private Expression titleExpression;

  private HashMap attributesMap = new HashMap();

  private List attributes = new ArrayList();

  private boolean secure;

  public Page(final String name, final String title, final String path,
    final boolean secure) {
    super(name);
    setTitle(title);
    setPath(path);
    this.secure = secure;
  }

  public Page(final Page page) {
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

  public void addArgument(final Argument argument) {
    if (!hasArgument(argument.getName())) {
      arguments.add(argument);
      argumentsMap.put(argument.getName(), argument);
    }
    if (argument.isInheritable()) {
      for (Iterator pages = this.pages.values().iterator(); pages.hasNext();) {
        Page page = (Page)pages.next();
        page.addArgument(argument);
      }
    }
  }

  public boolean hasArgument(final String name) {
    return argumentsMap.containsKey(name);
  }

  public void setLayout(final Layout layout) {
    this.layout = layout;
    layout.setPage(this);
  }

  public List getArguments() {
    return arguments;
  }

  public Layout getLayout() {
    return layout;
  }

  public void setMenuId(final long menuId) {
    this.menuId = menuId;
  }

  public long getMenuId() {
    return menuId;
  }

  public void setPath(final String path) {
    if (path != null) {
      if (!path.startsWith("/")) {
        this.path = "/" + path;
      } else {
        this.path = path;
      }
    } else {
      this.path = "/" + CaseConverter.toLowerCamelCase(getName());
    }
  }

  public String getPath() {
    return path;
  }

  public String getAbsolutePath() {
    if (parent != null) {
      return parent.getAbsolutePath() + path;
    } else {
      return WebUiContext.get().getConfig().getBasePath() + path;
    }
  }

  public String getFullPath() {
    if (secure) {
      return getAbsolutePath() + ".wps";
    } else {
      return getAbsolutePath() + ".wp";
    }
  }

  public String getFullUrl() {
    return getFullUrl(Collections.EMPTY_MAP);
  }

  public String getFullUrl(final Map parameters) {
    WebUiContext iafContext = WebUiContext.get();
    Map uriParameters = new HashMap(parameters);
    if (iafContext != null) {
      HttpServletRequest request = iafContext.getRequest();
      if (request != null) {
        for (Iterator arguments = this.arguments.iterator(); arguments.hasNext();) {
          Argument argument = (Argument)arguments.next();
          String name = argument.getName();
          if (!uriParameters.containsKey(name)) {
            String value = request.getParameter(name);
            if (value != null) {
              uriParameters.put(name, value);
            }
          }
        }
      }
    }
    return UrlUtil.getUrl(getFullPath(), uriParameters);
  }

  public final boolean isSecure() {
    return secure;
  }

  public final void setSecure(final boolean secure) {
    this.secure = secure;
  }

  public void setTitle(final String title) {
    if (title != null) {
      this.title = title;
      try {
        titleExpression = JexlUtil.createExpression(title);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      this.title = CaseConverter.toCapitalizedWords(getName());
    }
  }

  public String getTitle() {
    if (titleExpression != null) {
      WebUiContext context = WebUiContext.get();
      return (String)context.evaluateExpression(titleExpression);
    } else {
      return title;
    }
  }

  public void addProperty(final String name, final String value) {
    properties.put(name, value);
  }

  public String getProperty(final String name) {
    return (String)properties.get(name);
  }

  public Object clone() {
    return new Page(this);
  }

  public void addMenu(final Menu menu) {
    menus.put(menu.getName(), menu);
  }

  public Menu getMenu(final String name) {
    return (Menu)menus.get(name);
  }

  public void addPage(final Page page) {
    pages.put(page.getName(), page);
    pathMap.put(page.getPath(), page);
    page.setParent(this);
    for (Iterator entries = page.getPathMap().entrySet().iterator(); entries.hasNext();) {
      Map.Entry entry = (Map.Entry)entries.next();
      String path = page.getPath() + entry.getKey();
      Page childPage = (Page)entry.getValue();
      pathMap.put(path, childPage);
    }

    for (Iterator args = arguments.iterator(); args.hasNext();) {
      Argument argument = (Argument)args.next();
      if (argument.isInheritable()) {
        page.addArgument(argument);
      }
    }
    for (Iterator args = attributes.iterator(); args.hasNext();) {
      Attribute attribute = (Attribute)args.next();
      if (attribute.isInheritable()) {
        page.addAttribute(attribute);
      }
    }
  }

  public Page getPage(final String name) {
    if (name == null) {
      return null;
    }
    try {
      if (name.startsWith("/")) {
        return WebUiContext.get().getConfig().getPage(name);
      }
      Page page = (Page)pages.get(name);
      if (page == null) {
        Page parent = getParent();
        if (parent != null) {
          page = parent.getPage(name);
        } else {
          page = WebUiContext.get().getConfig().getPage("/" + name);
        }
      }
      return page;
    } catch (PageNotFoundException e) {
      return null;
    }
  }

  public Page getParent() {
    return parent;
  }

  public void setParent(final Page parent) {
    this.parent = parent;
    if (parent.isSecure()) {
      secure = true;
    }
  }

  public HashMap getPathMap() {
    return pathMap;
  }

  public void setPathMap(final HashMap pathMap) {
    this.pathMap = pathMap;
  }

  public boolean equals(final Object o) {
    if (o instanceof Page) {
      Page p = (Page)o;
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

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  public int hashCode() {
    return super.hashCode() + (path.hashCode() << 2);
  }

  public void addAttribute(final Attribute attribute) {
    if (!hasArgument(attribute.getName())) {
      attributes.add(attribute);
      attributesMap.put(attribute.getName(), attribute);
    }
    if (attribute.isInheritable()) {
      for (Iterator pages = this.pages.values().iterator(); pages.hasNext();) {
        Page page = (Page)pages.next();
        page.addAttribute(attribute);
      }
    }
  }

  public List getAttributes() {
    return attributes;
  }

  public boolean hasAttribute(final String name) {
    return attributesMap.containsKey(name);
  }
}
