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

  private String name;

  private String file;

  private HashMap fields = new HashMap();

  private Collection actions = new LinkedHashSet();

  private Collection styles = new LinkedHashSet();

  private Collection scripts = new LinkedHashSet();

  private Collection onLoads = new LinkedHashSet();

  private String area;

  public Component() {
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

  public Component(final Component component) {
    this.name = component.name;
    this.file = component.file;
    fields.putAll(component.fields);
    actions.addAll(component.actions);
    scripts.addAll(component.scripts);
    styles.addAll(component.styles);
    onLoads.addAll(component.onLoads);
  }

  public String getArea() {
    return area;
  }

  public void setArea(final String area) {
    this.area = area;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public String getFile() {
    return file;
  }

  public void addField(final Field field) {
    fields.put(field.getName(), EMPTY_STRING);
  }

  public void addField(final String name) {
    fields.put(name, EMPTY_STRING);
  }

  public void addField(final String name, final String value) {
    String lastValue = (String)fields.get(name);
    if (lastValue != EMPTY_STRING) {
      throw new IllegalArgumentException("Value already exists for the field "
        + name);
    }
    fields.put(name, value);
  }

  public String getField(final String name) {
    return null;
  }

  public void addAction(final ActionConfig actionConfig) {
    actions.add(actionConfig);
  }

  public void addActions(final Collection actionConfigs) {
    this.actions.addAll(actionConfigs);
  }

  public Collection getActions() {
    return actions;
  }

  /**
   * @param actions The actions to set.
   */
  public void setActions(Collection actions) {
    this.actions = actions;
  }

  public void invokeActions(final ServletContext context,
    final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    Iterator i = actions.iterator();
    while (i.hasNext()) {
      ActionConfig actionConfig = (ActionConfig)i.next();
      IafAction action = actionConfig.getAction();
      action.process(request, response);
    }
  }

  public void addStyle(final String style) {
    styles.add(style);
  }

  public void addStyle(final Style style) {
    styles.add(style.getFile());
  }

  public void addStyles(final Collection styles) {
    this.styles.addAll(styles);
  }

  public Collection getStyles() {
    return styles;
  }

  /**
   * @param styles The styles to set.
   */
  public void setStyles(Collection styles) {
    this.styles = styles;
  }

  public void addScript(final Script script) {
    scripts.add(script.getFile());
  }

  public void addScript(final String script) {
    scripts.add(script);
  }

  public void addScripts(final Collection scripts) {
    this.scripts.addAll(scripts);
  }

  public Collection getScripts() {
    return scripts;
  }

  /**
   * @param scripts The scripts to set.
   */
  public void setScripts(Collection scripts) {
    this.scripts = scripts;
  }

  public void addOnLoad(final OnLoad onLoad) {
    onLoads.add(onLoad.getScript());
  }

  public void addOnLoad(final String onLoad) {
    onLoads.add(onLoad);
  }

  public void addOnLoads(final Collection onLoads) {
    this.onLoads.addAll(onLoads);
  }

  public Collection getOnLoads() {
    return onLoads;
  }

  public void setPage(final Page page) {
    page.addActions(actions);
    page.addScripts(scripts);
    page.addStyles(styles);
    page.addOnLoads(onLoads);
  }

  public boolean equals(final Object o) {
    if (o instanceof Component) {
      Component c = (Component)o;
      if (equalsWithNull(c.name, name) && equalsWithNull(c.file, file)
        && c.styles.equals(styles) && c.scripts.equals(scripts)
        && c.onLoads.equals(onLoads)) {
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

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  public int hashCode() {
    if (name != null){
    return name.hashCode();
    } else {
      return super.hashCode();
    }
  }

  public Object clone() {
    return new Component(this);
  }

  public void includeComponent(final PageContext context)
    throws ServletException, IOException {
    context.getOut().flush();
    context.include(getFile());
  }
}
