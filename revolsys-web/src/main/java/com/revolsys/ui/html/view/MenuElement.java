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
import com.revolsys.util.Property;

public class MenuElement extends Element {
  private String cssClass = "menu";

  private JexlHttpServletRequestContext jexlContext;

  private Menu menu;

  private int numLevels = 1;

  private boolean showRoot = true;

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
    return this.cssClass;
  }

  /**
   * @return Returns the menu.
   */
  public Menu getMenu() {
    return this.menu;
  }

  /**
   * @return Returns the numLevels.
   */
  public int getNumLevels() {
    return this.numLevels;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    this.jexlContext = new JexlHttpServletRequestContext(request);
  }

  /**
   * @return Returns the showRoot.
   */
  public boolean isShowRoot() {
    return this.showRoot;
  }

  private void menu(final XmlWriter out, final Collection<Menu> items, final int level) {
    if (items.size() > 0) {
      out.startTag(HtmlElem.UL);
      for (final Menu menu : items) {
        if (menu.isVisible()) {
          out.startTag(HtmlElem.LI);

          final String cssClass = menu.getCssClass();
          if (cssClass != null) {
            out.attribute(HtmlAttr.CLASS, cssClass);
          }
          menuLink(out, menu);
          if (level < this.numLevels) {
            menu(out, menu.getMenus(), level + 1);
          }
          out.endTag(HtmlElem.LI);
        }
      }
      out.endTag(HtmlElem.UL);
    }
  }

  private void menuLink(final XmlWriter out, final Menu menu) {
    String uri = menu.getLink(this.jexlContext);
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

        out.text(linkTitle);
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
      if (this.showRoot || !menus.isEmpty()) {
        out.startTag(HtmlElem.DIV);
        out.attribute(HtmlAttr.CLASS, this.cssClass);

        if (this.showRoot && this.menu.getTitle() != null) {
          out.startTag(HtmlElem.DIV);
          out.attribute(HtmlAttr.CLASS, "title");
          menuLink(out, this.menu);
          out.endTag(HtmlElem.DIV);

        }
        menu(out, menus, 1);
        out.endTag(HtmlElem.DIV);
      }
    }
  }

  /**
   * @param cssClass The cssClass to set.
   */
  public void setCssClass(final String cssClass) {
    this.cssClass = cssClass;
  }

  /**
   * @param menu The menu to set.
   */
  public void setMenu(final Menu menu) {
    this.menu = menu;
  }

  /**
   * @param numLevels The numLevels to set.
   */
  public void setNumLevels(final int numLevels) {
    this.numLevels = numLevels;
  }

  /**
   * @param showRoot The showRoot to set.
   */
  public void setShowRoot(final boolean showRoot) {
    this.showRoot = showRoot;
  }
}
