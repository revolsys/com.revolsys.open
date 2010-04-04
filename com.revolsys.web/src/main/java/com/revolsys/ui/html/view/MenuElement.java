package com.revolsys.ui.html.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
import com.revolsys.xml.io.XmlWriter;

public class MenuElement extends Element {
  private String cssClass = "menu";

  private Menu menu;

  private int numLevels = 1;

  private boolean showRoot = true;

  private JexlHttpServletRequestContext jexlContext;

  public MenuElement() {
  }

  public MenuElement(final Menu menu, final String cssClass) {
    this.menu = menu;
    this.cssClass = cssClass;
  }

  /**
   * @return Returns the cssClass.
   */
  public String getCssClass() {
    return cssClass;
  }

  /**
   * @return Returns the menu.
   */
  public Menu getMenu() {
    return menu;
  }

  /**
   * @return Returns the numLevels.
   */
  public int getNumLevels() {
    return numLevels;
  }

  /**
   * @return Returns the showRoot.
   */
  public boolean isShowRoot() {
    return showRoot;
  }

  private void menu(final XmlWriter out, final Collection<Menu> items,
    final int level) throws IOException {
    // Collection items = menu.getItems();
    if (items.size() > 0) {
      out.startTag(HtmlUtil.UL);
      for (Menu menu : items) {
        if (menu.isVisible()) {
          out.startTag(HtmlUtil.LI);

          String cssClass = menu.getCssClass();
          if (cssClass != null) {
            out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
          }
          menuLink(out, menu);
          if (level < numLevels) {
            menu(out, menu.getMenus(), level + 1);
          }
          out.endTag(HtmlUtil.LI);
        }
      }
      out.endTag(HtmlUtil.UL);
    }
  }

  public void initialize(HttpServletRequest request) {
    this.jexlContext = new JexlHttpServletRequestContext(request);
  }

  private void menuLink(final XmlWriter out, final Menu menu)
    throws IOException {
    String uri = menu.getLink(jexlContext);
    String linkTitle = menu.getLinkTitle();
    String onClick = menu.getOnClick();
    if (onClick != null && uri == null) {
      uri = "#";
    }
    if (uri != null) {
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, uri);
      out.attribute(HtmlUtil.ATTR_TITLE, linkTitle);
      out.attribute(HtmlUtil.ATTR_ON_CLICK, onClick);

      out.text(linkTitle);
      out.endTag(HtmlUtil.A);
    } else {
      out.text(linkTitle);
    }
  }

  public void serializeElement(final XmlWriter out) throws IOException {
    if (menu != null) {
      List<Menu> menus = new ArrayList<Menu>();
      for (Menu menuItem : menu.getMenus()) {
        if (menuItem.isVisible()) {
          menus.add(menuItem);
        }
      }
      if (showRoot || !menus.isEmpty()) {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, cssClass);

        if (showRoot && menu.getTitle() != null) {
          out.startTag(HtmlUtil.DIV);
          out.attribute(HtmlUtil.ATTR_CLASS, "title");
          menuLink(out, menu);
          out.endTag(HtmlUtil.DIV);

        }
        menu(out, menus, 1);
        out.endTag(HtmlUtil.DIV);
      }
    }
  }

  /**
   * @param cssClass The cssClass to set.
   */
  public void setCssClass(String cssClass) {
    this.cssClass = cssClass;
  }

  /**
   * @param menu The menu to set.
   */
  public void setMenu(Menu menu) {
    this.menu = menu;
  }

  /**
   * @param numLevels The numLevels to set.
   */
  public void setNumLevels(int numLevels) {
    this.numLevels = numLevels;
  }

  /**
   * @param showRoot The showRoot to set.
   */
  public void setShowRoot(boolean showRoot) {
    this.showRoot = showRoot;
  }
}
