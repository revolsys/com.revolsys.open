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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.web.exception.PageNotFoundException;

public class Config implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(Config.class);

  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 4510031487114008232L;

  private String basePath = "";

  private final Map components = new HashMap();

  private final Map layouts = new HashMap();

  private final Map menus = new HashMap();

  private final Map<String, Page> pageByName = new HashMap<>();

  private final Map<Pattern, Page> pageByPattern = new HashMap<>();

  private final Map pagePathMap = new HashMap();

  private final List<Pattern> pagePatterns = new ArrayList<>();

  private final Map<String, Page> pages = new HashMap<>();

  private final ServletContext servletContext;

  public Config(final ServletContext servletContext, final String basePath) {
    this.servletContext = servletContext;
    setBasePath(basePath);
  }

  /**
   * <p>
   * Add the Component to the list of components, the name property will be
   * assoiciated with the Component and can be used to look up the Component.
   * </p>
   *
   * @param component The Component to add @ If another Component with the same
   *          name has already been defined
   */
  public void addComponent(final Component component) {
    final String name = component.getName();
    if (this.components.containsKey(name)) {
      throw new IllegalArgumentException(
        new StringBuilder("Duplicate Component definition with name ").append(name).toString());
    }
    this.components.put(name, component);
  }

  /**
   * @param layout
   */
  public void addLayout(final Layout layout) {
    addComponent(layout);
    final String name = layout.getName();
    if (this.layouts.containsKey(name)) {
      throw new IllegalArgumentException(
        new StringBuilder("Duplicate Layout definition with name ").append(name).toString());
    }
    this.layouts.put(name, layout);
  }

  public void addMenu(final Menu menu) {
    final String name = menu.getName();
    if (this.menus.containsKey(name)) {
      throw new IllegalArgumentException(
        new StringBuilder("Duplicate Menu definition with name ").append(name).toString());
    }
    this.menus.put(name, menu);
  }

  /**
   * @param page
   */
  public void addPage(final Page page) {
    final String path = page.getAbsolutePath();
    // if (pages.containsKey(path)) {
    // throw new IllegalArgumentException("Duplicate Page definition with path="
    // + path);
    // }
    final String name = page.getName();
    // if (pageByName.containsKey(name)) {
    // throw new IllegalArgumentException("Duplicate Page definition with name "
    // + name);
    // }
    this.pages.put(path, page);
    this.pageByName.put(name, page);
    this.pagePathMap.put(page, path);
    if (path.indexOf("*") != -1) {
      final Pattern pathPattern = Pattern.compile(path);
      this.pagePatterns.add(pathPattern);
      this.pageByPattern.put(pathPattern, page);

    }
    for (final Object element : page.getPathMap().entrySet()) {
      final Map.Entry entry = (Map.Entry)element;
      final String fullPath = page.getPath() + entry.getKey();
      final Page childPage = (Page)entry.getValue();
      // if (pages.containsKey(fullPath)) {
      // throw new IllegalArgumentException(
      // "Duplicate Page definition with path=" + fullPath);
      // }
      this.pages.put(fullPath, childPage);
      this.pagePathMap.put(childPage, fullPath);
    }
  }

  /**
   * <p>
   * Check to see if two objects are equal. Both objects must have the same
   * components, layouts, pages and menu tree to be equal.
   * </p>
   *
   * @param o The object to compare this object with
   * @return true if both objects are equal
   */
  @Override
  public boolean equals(final Object o) {
    if (o instanceof Config) {
      final Config c = (Config)o;
      if (c.components.equals(this.components) && c.layouts.equals(this.layouts)
        && c.pages.equals(this.pages) && c.menus.equals(this.menus)) {
        return true;
      }
    }
    return false;
  }

  public String getBasePath() {
    return this.basePath;
  }

  /**
   * @param name
   * @return
   */
  public Component getComponent(final String name) {
    final Component component = (Component)this.components.get(name);
    if (component == null) {
      throw new IllegalArgumentException(
        new StringBuilder("There does not exist a Component definition with name '").append(name)
          .append("'")
          .toString());
    }
    return component;
  }

  /**
   * @param name
   * @return
   */
  public Layout getLayout(final String name) {
    final Layout layout = (Layout)this.layouts.get(name);
    if (layout == null) {
      throw new IllegalArgumentException(
        new StringBuilder("There does not exist a Component definition with name '").append(name)
          .append("'")
          .toString());
    }
    return layout;
  }

  public Menu getMenu(final String name) {
    final Menu menu = (Menu)this.menus.get(name);
    return menu;
  }

  public Map getMenus() {
    return this.menus;
  }

  public Page getPage(final String path) throws PageNotFoundException {
    Page page = this.pages.get(path);
    if (page != null) {
      return page;
    } else {
      // Check to see if there is a page match for any of the parent paths
      String parentPath = path;
      for (int pathIndex = parentPath.lastIndexOf('/'); pathIndex != -1; pathIndex = parentPath
        .lastIndexOf('/')) {
        parentPath = parentPath.substring(0, pathIndex);
        page = this.pages.get(parentPath);
        if (page != null) {
          return page;
        }
      }
      // Check to see if there is a page match using a regular expression
      for (final Pattern pattern : this.pagePatterns) {
        final Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
          return this.pageByPattern.get(pattern);
        }
      }
      throw new PageNotFoundException();
    }
  }

  public Page getPageByName(final String pageName) throws PageNotFoundException {
    final Page page = this.pageByName.get(pageName);
    if (page == null) {
      throw new PageNotFoundException("Page " + pageName + " does not exist");
    }
    return page;
  }

  public String getPath(final Page page) {
    return (String)this.pagePathMap.get(page);
  }

  public ServletContext getServletContext() {
    return this.servletContext;
  }

  /**
   * Generate the hash code for the object.
   *
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return this.components.hashCode() + (this.layouts.hashCode() << 2)
      + (this.pages.hashCode() << 4) + (this.menus.hashCode() << 6);
  }

  public void setBasePath(final String basePath) {
    if (basePath != null) {
      this.basePath = basePath;
    } else {
      this.basePath = "";
    }
  }
}
