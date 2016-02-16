package com.revolsys.ui.html.view;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.model.MenuBar;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class BootstrapUtil {

  public static void icon(final XmlWriter writer, final String icon) {
    writer.startTag(HtmlUtil.SPAN);
    writer.attribute(HtmlUtil.ATTR_CLASS, icon);
    writer.attribute("aria-hidden", true);
    writer.text("");
    writer.endTag(HtmlUtil.SPAN);
  }

  private static void navBarBrand(final XmlWriter writer, final String title, final String imageSrc,
    final String uri) {
    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_CLASS, "navbar-brand");
    writer.attribute(HtmlUtil.ATTR_HREF, uri);
    if (Property.hasValue(imageSrc)) {
      writer.attribute(HtmlUtil.ATTR_TITLE, title);
      HtmlUtil.serializeImage(writer, imageSrc, title);
    }
    writer.text(title);
    writer.endTag(HtmlUtil.A);
  }

  public static void navbarDropdownEnd(final XmlWriter writer) {
    writer.endTag(HtmlUtil.UL);
    writer.endTag(HtmlUtil.LI);
  }

  public static void navbarDropdownStart(final XmlWriter writer, final Menu menu) {
    writer.startTag(HtmlUtil.LI);
    writer.attribute(HtmlUtil.ATTR_CLASS, "dropdown");

    writer.startTag(HtmlUtil.A);
    writer.attribute(HtmlUtil.ATTR_CLASS, "dropdown-toggle");
    writer.attribute("data-toggle", "dropdown");
    writer.attribute(HtmlUtil.ATTR_ROLE, "button");
    writer.attribute("aria-expanded", "false");
    writer.text(menu.getTitle());
    writer.text(" ");
    icon(writer, "caret");
    writer.endTag(HtmlUtil.A);

    writer.startTag(HtmlUtil.UL);
    writer.attribute(HtmlUtil.ATTR_CLASS, "dropdown-menu");
    writer.attribute(HtmlUtil.ATTR_ROLE, "menu");
  }

  public static void navbarEnd(final XmlWriter writer) {
    writer.endTagLn(HtmlUtil.UL);
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.DIV);
    writer.endTagLn(HtmlUtil.NAV);
  }

  public static void navbarStart(final XmlWriter writer, final String navbarClass,
    final String navMenuClass, final Menu menu, final JexlHttpServletRequestContext jexlContext) {

    writer.startTag(HtmlUtil.NAV);

    final String id = menu.getId();
    writer.attribute(HtmlUtil.ATTR_ID, id);
    writer.attribute(HtmlUtil.ATTR_CLASS, "navbar navbar-default " + navbarClass);
    writer.newLine();

    if (menu instanceof MenuBar) {
      final MenuBar menuBar = (MenuBar)menu;
      final String organizationName = menuBar.getOrganizationName();
      final String organizationImageSrc = menuBar.getOrganizationImageSrc();
      final String organizationUri = menuBar.getOrganizationUri();
      if (Property.hasValue(organizationImageSrc)) {
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "navbar-left");
        writer.startTag(HtmlUtil.A);
        writer.attribute(HtmlUtil.ATTR_HREF, organizationUri);
        HtmlUtil.serializeImage(writer, organizationImageSrc, organizationName);
        writer.endTag(HtmlUtil.A);
        writer.endTag(HtmlUtil.DIV);

      } else if (Property.hasValue(organizationName)) {
        navBarBrand(writer, organizationName, null, organizationUri);
      }
    }
    writer.startTag(HtmlUtil.DIV);
    writer.attribute(HtmlUtil.ATTR_CLASS, "container");
    writer.newLine();

    {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "navbar-header");
      writer.newLine();
      {
        writer.startTag(HtmlUtil.BUTTON);
        writer.attribute(HtmlUtil.ATTR_TYPE, "button");
        writer.attribute(HtmlUtil.ATTR_CLASS, "navbar-toggle collapsed");
        writer.attribute("data-toggle", "collapse");
        writer.attribute("data-target", "#" + id + "Bar");
        writer.attribute("aria-expanded", "false");
        writer.attribute("aria-controls", "navbar");
        writer.newLine();

        HtmlUtil.serializeSpan(writer, "sr-only", "Toggle navigation");

        for (int i = 0; i < 3; i++) {
          icon(writer, "icon-bar");
        }
        writer.endTagLn(HtmlUtil.BUTTON);

        final String title = menu.getTitle();
        final String imageSrc = menu.getImageSrc();
        final String uri = menu.getLink(jexlContext);
        if (Property.hasValuesAny(imageSrc, title)) {
          navBarBrand(writer, title, imageSrc, uri);
        }
        writer.endTagLn(HtmlUtil.DIV);
      }
      {
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_ID, id + "Bar");
        writer.attribute(HtmlUtil.ATTR_CLASS, "navbar-collapse collapse");
        writer.attribute("aria-expanded", "false");
        writer.newLine();

        writer.startTag(HtmlUtil.UL);
        writer.attribute(HtmlUtil.ATTR_CLASS, "nav navbar-nav " + navMenuClass);

      }
    }

  }
}
