package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class ButtonsToolbarElement extends Element {
  private JexlHttpServletRequestContext jexlContext;

  private final Menu menu;

  public ButtonsToolbarElement(final Menu menu) {
    this.menu = menu;
  }

  public Menu getMenu() {
    return this.menu;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    this.jexlContext = new JexlHttpServletRequestContext(request);
  }

  private void menu(final XmlWriter out, final Collection<Menu> items, final int level) {
    if (items.size() > 0) {
      for (final Menu menu : items) {
        if (menu.isVisible()) {
          menuLink(out, menu);
          final List<Menu> menus = menu.getMenus();
          menu(out, menus, level + 1);
        }
      }
    }
  }

  private void menuLink(final XmlWriter out, final Menu menu) {
    String uri = menu.getLink(this.jexlContext);
    String linkTitle = menu.getLinkTitle();
    final String onClick = menu.getOnClick();
    if (onClick != null && uri == null) {
      uri = "#";
    }
    final String buttonClass = menu.getProperty("buttonClass", "btn-default");
    if (Property.hasValue(uri)) {
      if (uri.startsWith("javascript:")) {
        out.startTag(HtmlElem.BUTTON);
        out.attribute(HtmlAttr.CLASS, "btn btn-sm " + buttonClass);
        out.attribute(HtmlAttr.ON_CLICK, uri.substring(11));
        linkTitle = menu.getTitle();
        final String iconName = menu.getIconName();
        if (Property.hasValue(iconName)) {
          BootstrapUtil.icon(out, iconName);
          HtmlUtil.serializeSpan(out, "sr-only", linkTitle);
        } else {
          out.text(linkTitle);
        }
        out.endTag(HtmlElem.BUTTON);
      } else {
        out.startTag(HtmlElem.A);
        out.attribute(HtmlAttr.HREF, uri);
        out.attribute(HtmlAttr.TITLE, linkTitle);
        out.attribute(HtmlAttr.ON_CLICK, onClick);
        out.attribute(HtmlAttr.TARGET, menu.getTarget());
        out.attribute(HtmlAttr.CLASS, "btn btn-sm " + buttonClass);
        out.attribute(HtmlAttr.ROLE, "button");

        final String iconName = menu.getIconName();
        if (Property.hasValue(iconName)) {
          BootstrapUtil.icon(out, iconName);
          HtmlUtil.serializeSpan(out, "sr-only", linkTitle);
        } else {
          out.text(linkTitle);
        }
        out.endTag(HtmlElem.A);
      }
    } else {
      out.text(linkTitle);
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.menu != null) {
      final List<Menu> menus = new ArrayList<>();
      for (final Menu menuItem : this.menu.getMenus()) {
        if (menuItem.isVisible()) {
          menus.add(menuItem);
        }
      }
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "btn-toolbar");
      out.attribute(HtmlAttr.ROLE, "toolbar");

      menu(out, menus, 1);
      out.endTag(HtmlElem.DIV);
    }
  }

}
