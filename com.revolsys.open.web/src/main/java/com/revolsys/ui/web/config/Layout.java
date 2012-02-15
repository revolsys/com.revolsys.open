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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

public class Layout extends Component {

  private boolean page;

  private final Set areas = new HashSet();

  private final Map components = new HashMap();

  public Layout() {
  }

  public Layout(final Layout layout) {
    super(layout);
    page = layout.page;
    areas.addAll(layout.areas);
    final Iterator keys = layout.components.keySet().iterator();
    while (keys.hasNext()) {
      final String key = (String)keys.next();
      final Component component = (Component)layout.components.get(key);
      components.put(key, component.clone());
    }
  }

  public Layout(final String area, final String name, final String file,
    final boolean page) {
    super(area, name, file);
    this.page = page;
  }

  public void addArea(final Area area) {
    areas.add(area.getName());
  }

  public void addArea(final String name) {
    areas.add(name);
  }

  @Override
  public Object clone() {
    return new Layout(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Layout) {
      final Layout l = (Layout)o;
      if (super.equals(o) && l.page == page && l.areas.equals(areas)
        && l.components.equals(components)) {
        return true;
      }
    }
    return false;
  }

  public Component getArea(final String name) {
    return null;
  }

  public Component getComponent(final String name) {
    return (Component)components.get(name);
  }

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public void includeComponent(final PageContext context) throws IOException,
    ServletException {
    final WebUiContext niceContext = WebUiContext.get();
    niceContext.pushLayout(this);
    context.getOut().flush();
    context.include(getFile());
    niceContext.popLayout();
  }

  public boolean isPage() {
    return page;
  }

  public void setComponent(final String name, final Component component) {
    if (!areas.contains(name)) {
      throw new IllegalArgumentException(new StringBuffer(
        "Area does not exist with name ").append(name).toString());
    }
    components.put(name, component);
  }

  /**
   * @param page The page to set.
   */
  public void setPage(final boolean page) {
    this.page = page;
  }

  @Override
  public void setPage(final Page page) {
    super.setPage(page);
    final Iterator children = components.values().iterator();
    while (children.hasNext()) {
      final Component component = (Component)children.next();
      component.setPage(page);
    }
  }
}
