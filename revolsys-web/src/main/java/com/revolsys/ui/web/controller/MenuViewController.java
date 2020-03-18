package com.revolsys.ui.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl3.JexlContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.BootstrapUtil;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.model.Navbar;
import com.revolsys.ui.web.annotation.RequestMapping;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

@Controller
public class MenuViewController {
  private void bootstrapMenu(final XmlWriter writer, final Collection<Menu> items, final int level,
    final JexlContext jexlContext) {
    if (items.size() > 0) {
      for (final Menu menu : items) {
        if (menu.isVisible()) {
          final List<Menu> menus = menu.getMenus();
          if (menus.isEmpty()) {
            writer.startTag(HtmlElem.LI);
            final String cssClass = menu.getCssClass();
            if (cssClass != null) {
              writer.attribute(HtmlAttr.CLASS, cssClass);
            }
            bootStrapMenuLink(writer, menu, jexlContext);
            writer.endTag(HtmlElem.LI);
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
        out.startTag(HtmlElem.BUTTON);
        out.attribute(HtmlAttr.ON_CLICK, uri.substring(11));
        out.text(menu.getTitle());
        out.endTag(HtmlElem.BUTTON);
      } else {
        out.startTag(HtmlElem.A);
        out.attribute(HtmlAttr.HREF, uri);
        out.attribute(HtmlAttr.TITLE, linkTitle);
        out.attribute(HtmlAttr.ON_CLICK, onClick);
        out.attribute(HtmlAttr.TARGET, menu.getTarget());
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

  private void bootstrapNavbar(final HttpServletRequest request, final HttpServletResponse response,
    final Navbar navBar) throws IOException {
    if (navBar != null) {
      try (
        final OutputStream out = response.getOutputStream();
        XmlWriter writer = new XmlWriter(out, false)) {
        writer.setIndent(false);
        final JexlHttpServletRequestContext jexlContext = new JexlHttpServletRequestContext(
          request);
        final List<Menu> menus = new ArrayList<>();
        for (final Menu menuItem : navBar.getMenus()) {
          if (menuItem.isVisible()) {
            menus.add(menuItem);
          }
        }
        final String title = navBar.getTitle();
        if (Property.hasValue(title) || !menus.isEmpty()) {
          BootstrapUtil.navbarStart(writer, navBar, jexlContext);
          bootstrapMenu(writer, menus, 1, jexlContext);
          BootstrapUtil.navbarEnd(writer);
        }
        writer.flush();
      }
    }

  }

  @RequestMapping("/view/footer/{menuName}")
  public void footer(final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable("menuName") final String menuName) throws IOException {
    final Navbar navbar = (Navbar)request.getAttribute(menuName);
    bootstrapNavbar(request, response, navbar);
  }

  @RequestMapping("/view/header/{menuName}")
  public void header(final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable("menuName") final String menuName) throws IOException {
    final Navbar navbar = (Navbar)request.getAttribute(menuName);
    bootstrapNavbar(request, response, navbar);
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
