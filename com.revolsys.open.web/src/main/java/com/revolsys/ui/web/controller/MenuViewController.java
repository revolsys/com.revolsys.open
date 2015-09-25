package com.revolsys.ui.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl.JexlContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.BootstrapUtil;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

@Controller
public class MenuViewController {

  private void bootstrapMenu(final HttpServletRequest request, final HttpServletResponse response,
    final Menu menu, final String navbarClass, final String navMenuClass) throws IOException {
    if (menu != null) {
      try (
        final OutputStream out = response.getOutputStream();
        XmlWriter writer = new XmlWriter(out)) {
        writer.setIndent(false);
        final JexlHttpServletRequestContext jexlContext = new JexlHttpServletRequestContext(
          request);
        final List<Menu> menus = new ArrayList<Menu>();
        for (final Menu menuItem : menu.getMenus()) {
          if (menuItem.isVisible()) {
            menus.add(menuItem);
          }
        }
        final String title = menu.getTitle();
        if (Property.hasValue(title) || !menus.isEmpty()) {
          final String uri = menu.getLink(jexlContext);
          BootstrapUtil.navbarStart(writer, menu.getId(), navbarClass, navMenuClass, title, uri);
          bootstrapMenu(writer, menus, 1, jexlContext);
          BootstrapUtil.navbarEnd(writer);
        }
        writer.flush();
      }
    }

  }

  private void bootstrapMenu(final XmlWriter writer, final Collection<Menu> items, final int level,
    final JexlContext jexlContext) {
    if (items.size() > 0) {
      for (final Menu menu : items) {
        if (menu.isVisible()) {
          final List<Menu> menus = menu.getMenus();
          if (menus.isEmpty()) {
            writer.startTag(HtmlUtil.LI);
            final String cssClass = menu.getCssClass();
            if (cssClass != null) {
              writer.attribute(HtmlUtil.ATTR_CLASS, cssClass);
            }
            bootStrapMenuLink(writer, menu, jexlContext);
            writer.endTag(HtmlUtil.LI);
          } else {
            BootstrapUtil.navbarDropdownStart(writer, menu);
            bootstrapMenu(writer, menus, level + 1, jexlContext);
            BootstrapUtil.navbarDropdownEnd(writer);
          }

        }
      }
    }
  }

  private void bootStrapMenuLink(final XmlWriter out, final Menu menu,
    final JexlContext jexlContext) {
    String uri = menu.getLink(jexlContext);
    final String linkTitle = menu.getLinkTitle();
    final String onClick = menu.getOnClick();
    if (onClick != null && uri == null) {
      uri = "#";
    }
    if (Property.hasValue(uri)) {
      if (uri.startsWith("javascript:")) {
        out.startTag(HtmlUtil.BUTTON);
        out.attribute(HtmlUtil.ATTR_ON_CLICK, uri.substring(11));
        out.text(menu.getTitle());
        out.endTag(HtmlUtil.BUTTON);
      } else {
        out.startTag(HtmlUtil.A);
        out.attribute(HtmlUtil.ATTR_HREF, uri);
        out.attribute(HtmlUtil.ATTR_TITLE, linkTitle);
        out.attribute(HtmlUtil.ATTR_ON_CLICK, onClick);
        out.attribute(HtmlUtil.ATTR_TARGET, menu.getTarget());
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

  @RequestMapping("/view/footer/{menuName}")
  public void footer(final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable("menuName") final String menuName) throws IOException {
    final Menu menu = (Menu)request.getAttribute(menuName);
    bootstrapMenu(request, response, menu, "navbar-fixed-bottom", "navbar-right");
  }

  @RequestMapping("/view/header/{menuName}")
  public void header(final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable("menuName") final String menuName) throws IOException {
    final Menu menu = (Menu)request.getAttribute(menuName);
    bootstrapMenu(request, response, menu, "navbar-fixed-top", "navbar-right");
  }

  @RequestMapping("/view/menu/{menuName}")
  public void menu(final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable("menuName") final String menuName) throws IOException {
    final Menu menu = (Menu)request.getAttribute(menuName);
    if (menu != null) {
      final MenuElement menuElement = new MenuElement(menu, menuName);
      final OutputStream out = response.getOutputStream();
      menuElement.serialize(out);
    }
  }

}
