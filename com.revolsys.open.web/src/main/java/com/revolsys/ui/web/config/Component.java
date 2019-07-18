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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.html.view.Style;

public class Component {
  private static final String EMPTY_STRING = "";

  private Collection actions = new LinkedHashSet();

  private String area;

  private final HashMap fields = new HashMap();

  private String file;

  private String name;

  private final Collection onLoads = new LinkedHashSet();

  private Collection scripts = new LinkedHashSet();

  private Collection styles = new LinkedHashSet();

  public Component() {
  }

  public Component(final Component component) {
    this.name = component.name;
    this.file = component.file;
    this.fields.putAll(component.fields);
    this.actions.addAll(component.actions);
    this.scripts.addAll(component.scripts);
    this.styles.addAll(component.styles);
    this.onLoads.addAll(component.onLoads);
  }

  public Component(final String name) {
    this.name = name;
  }

  public Component(final String area, final String name) {
    this(name);
    this.area = area;
  }

  public Component(final String area, final String name, final String file) {
    this(area, name);
    this.file = file;
  }

  public void addAction(final ActionConfig actionConfig) {
    this.actions.add(actionConfig);
  }

  public void addActions(final Collection actionConfigs) {
    this.actions.addAll(actionConfigs);
  }

  public void addField(final Field field) {
    this.fields.put(field.getName(), EMPTY_STRING);
  }

  public void addField(final String name) {
    this.fields.put(name, EMPTY_STRING);
  }

  public void addField(final String name, final String value) {
    final String lastValue = (String)this.fields.get(name);
    if (lastValue != EMPTY_STRING) {
      throw new IllegalArgumentException("Value already exists for the field " + name);
    }
    this.fields.put(name, value);
  }

  public void addOnLoad(final OnLoad onLoad) {
    this.onLoads.add(onLoad.getScript());
  }

  public void addOnLoad(final String onLoad) {
    this.onLoads.add(onLoad);
  }

  public void addOnLoads(final Collection onLoads) {
    this.onLoads.addAll(onLoads);
  }

  public void addScript(final Script script) {
    this.scripts.add(script.getFile());
  }

  public void addScript(final String script) {
    this.scripts.add(script);
  }

  public void addScripts(final Collection scripts) {
    this.scripts.addAll(scripts);
  }

  public void addStyle(final String style) {
    this.styles.add(style);
  }

  public void addStyle(final Style style) {
    this.styles.add(style.getFile());
  }

  public void addStyles(final Collection styles) {
    this.styles.addAll(styles);
  }

  @Override
  public Object clone() {
    return new Component(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Component) {
      final Component c = (Component)o;
      if (equalsWithNull(c.name, this.name) && equalsWithNull(c.file, this.file)
        && c.styles.equals(this.styles) && c.scripts.equals(this.scripts)
        && c.onLoads.equals(this.onLoads)) {
        return true;
      }
    }
    return false;
  }

  protected boolean equalsWithNull(final String value1, final String value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  public Collection getActions() {
    return this.actions;
  }

  public String getArea() {
    return this.area;
  }

  public String getField(final String name) {
    return null;
  }

  public String getFile() {
    return this.file;
  }

  public String getName() {
    return this.name;
  }

  public Collection getOnLoads() {
    return this.onLoads;
  }

  public Collection getScripts() {
    return this.scripts;
  }

  public Collection getStyles() {
    return this.styles;
  }

  /**
   * Generate the hash code for the object.
   *
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    if (this.name != null) {
      return this.name.hashCode();
    } else {
      return super.hashCode();
    }
  }

  public void includeComponent(final PageContext context) throws ServletException, IOException {
    context.getOut().flush();
    context.include(getFile());
  }

  public void invokeActions(final ServletContext context, final HttpServletRequest request,
    final HttpServletResponse response) throws ServletException, IOException {
    final Iterator i = this.actions.iterator();
    while (i.hasNext()) {
      final ActionConfig actionConfig = (ActionConfig)i.next();
      final IafAction action = actionConfig.getAction();
      action.process(request, response);
    }
  }

  /**
   * @param actions The actions to set.
   */
  public void setActions(final Collection actions) {
    this.actions = actions;
  }

  public void setArea(final String area) {
    this.area = area;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setPage(final Page page) {
    page.addActions(this.actions);
    page.addScripts(this.scripts);
    page.addStyles(this.styles);
    page.addOnLoads(this.onLoads);
  }

  /**
   * @param scripts The scripts to set.
   */
  public void setScripts(final Collection scripts) {
    this.scripts = scripts;
  }

  /**
   * @param styles The styles to set.
   */
  public void setStyles(final Collection styles) {
    this.styles = styles;
  }
}
