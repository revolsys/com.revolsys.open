package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
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
        out.startTag(HtmlUtil.BUTTON);
        out.attribute(HtmlUtil.ATTR_CLASS, "btn btn-sm " + buttonClass);
        out.attribute(HtmlUtil.ATTR_ON_CLICK, uri.substring(11));
        linkTitle = menu.getTitle();
        final String iconName = menu.getIconName();
        if (Property.hasValue(iconName)) {
          BootstrapUtil.icon(out, iconName);
          HtmlUtil.serializeSpan(out, "sr-only", linkTitle);
        } else {
          out.text(linkTitle);
        }
        out.endTag(HtmlUtil.BUTTON);
      } else {
        out.startTag(HtmlUtil.A);
        out.attribute(HtmlUtil.ATTR_HREF, uri);
        out.attribute(HtmlUtil.ATTR_TITLE, linkTitle);
        out.attribute(HtmlUtil.ATTR_ON_CLICK, onClick);
        out.attribute(HtmlUtil.ATTR_TARGET, menu.getTarget());
        out.attribute(HtmlUtil.ATTR_CLASS, "btn btn-sm " + buttonClass);
        out.attribute(HtmlUtil.ATTR_ROLE, "button");

        final String iconName = menu.getIconName();
        if (Property.hasValue(iconName)) {
          BootstrapUtil.icon(out, iconName);
          HtmlUtil.serializeSpan(out, "sr-only", linkTitle);
        } else {
          out.text(linkTitle);
        }
        out.endTag(HtmlUtil.A);
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
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "btn-toolbar");
      out.attribute(HtmlUtil.ATTR_ROLE, "toolbar");

      menu(out, menus, 1);
      out.endTag(HtmlUtil.DIV);
    }
  }

}
