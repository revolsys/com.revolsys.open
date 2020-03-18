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
package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.web.config.Menu;
import com.revolsys.ui.web.config.MenuItem;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class MenuView extends ObjectView {
  private static final Logger log = LoggerFactory.getLogger(MenuView.class);

  private String cssClass = "menu";

  private int numLevels = 1;

  private boolean showRoot;

  private void menu(final XmlWriter out, final Collection items, final int level) {
    // Collection items = menu.getItems();
    if (items.size() > 0) {
      out.startTag(HtmlElem.UL);
      for (final Iterator menuItemIter = items.iterator(); menuItemIter.hasNext();) {
        final MenuItem menuItem = (MenuItem)menuItemIter.next();
        if (menuItem.isVisible()) {
          out.startTag(HtmlElem.LI);

          final String cssClass = menuItem.getProperty("cssClass");
          if (cssClass != null) {
            out.attribute(HtmlAttr.CLASS, cssClass);
          }
          menuItemLink(out, menuItem);
          if (level < this.numLevels && menuItem instanceof Menu) {
            menu(out, ((Menu)menuItem).getItems(), level + 1);
          }
          out.endTag(HtmlElem.LI);
        }
      }
      out.endTag(HtmlElem.UL);
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "end");
      out.entityRef("nbsp");
      out.endTag(HtmlElem.DIV);
    }
  }

  private void menuItemLink(final XmlWriter out, final MenuItem menuItem) {

    final String uri = menuItem.getUri();
    if (Property.hasValue(uri)) {
      if (uri.startsWith("javascript:")) {
        out.startTag(HtmlElem.BUTTON);
        out.attribute(HtmlAttr.ON_CLICK, uri.substring(11));
        out.text(menuItem.getTitle());
        out.endTag(HtmlElem.BUTTON);
      } else {
        out.startTag(HtmlElem.A);
        out.attribute(HtmlAttr.HREF, uri);
        out.attribute(HtmlAttr.TITLE, menuItem.getTitle());
        out.text(menuItem.getTitle());
        out.endTag(HtmlElem.A);
      }
    } else {
      out.text(menuItem.getTitle());
    }
  }

  @Override
  public void processProperty(final String name, final Object value) {
    final String stringValue = (String)value;
    if (name.equals("cssClass")) {
      this.cssClass = value.toString();
    } else if (name.equals("numLevels")) {
      this.numLevels = Integer.parseInt(stringValue);
    } else if (name.equals("menuName")) {
      final WebUiContext context = WebUiContext.get();
      setObject(context.getMenu(stringValue));
      if (getObject() == null) {
        throw new IllegalArgumentException("Menu " + value + " does not exist");
      }
    } else if (name.equals("showRoot")) {
      this.showRoot = Boolean.valueOf(stringValue).booleanValue();
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final Menu menu = (Menu)getObject();
    if (menu != null) {
      final List menuItems = new ArrayList();
      for (final Object element : menu.getItems()) {
        final MenuItem menuItem = (MenuItem)element;
        if (menuItem.isVisible()) {
          menuItems.add(menuItem);
        }
      }
      if (this.showRoot || !menuItems.isEmpty()) {
        out.startTag(HtmlElem.DIV);
        out.attribute(HtmlAttr.CLASS, this.cssClass);

        if (this.showRoot) {
          out.startTag(HtmlElem.DIV);
          out.attribute(HtmlAttr.CLASS, "title");
          menuItemLink(out, menu);
          out.endTag(HtmlElem.DIV);

        }

        menu(out, menuItems, 1);

        out.endTag(HtmlElem.DIV);
      }
    }
  }
}
