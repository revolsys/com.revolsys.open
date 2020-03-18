package com.revolsys.ui.html.view;

import java.util.List;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.model.Brand;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.model.Navbar;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
import com.revolsys.ui.web.controller.PathAliasController;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class BootstrapUtil {

  public static void icon(final XmlWriter writer, final String icon) {
    writer.startTag(HtmlElem.SPAN);
    writer.attribute(HtmlAttr.CLASS, icon);
    writer.attribute("aria-hidden", true);
    writer.text("");
    writer.endTag(HtmlElem.SPAN);
  }

  private static void navBarBrand(final XmlWriter writer, final List<Brand> brands) {
    if (Property.hasValue(brands)) {
      writer.startTag(HtmlElem.DIV);
      writer.attribute(HtmlAttr.CLASS, "navbar-brand");
      for (final Brand brand : brands) {
        final String title = brand.getBrandTitle();
        String uri = brand.getBrandUri();
        uri = HttpServletUtils.getAbsoluteUrl(PathAliasController.getPath(uri));
        final String imageSrc = brand.getBrandImageSrc();
        final String smallImageSrc = brand.getBrandSmallImageSrc();
        final boolean hasUri = Property.hasValue(uri);
        if (hasUri) {
          writer.startTag(HtmlElem.A);
          writer.attribute(HtmlAttr.HREF, uri);
        }
        HtmlUtil.serializeImage(writer, imageSrc, title, "hidden-xs");
        HtmlUtil.serializeImage(writer, smallImageSrc, title, "visible-xs-inline-block");
        HtmlUtil.serializeSpan(writer, "navbar-brand-title", title);
        if (hasUri) {
          writer.endTag(HtmlElem.A);
        }
      }
      writer.endTag(HtmlElem.DIV);
    }
  }

  public static void navbarDropdownEnd(final XmlWriter writer) {
    writer.endTag(HtmlElem.UL);
    writer.endTag(HtmlElem.LI);
  }

  public static void navbarDropdownStart(final XmlWriter writer, final Menu menu) {
    writer.startTag(HtmlElem.LI);
    writer.attribute(HtmlAttr.CLASS, "dropdown");

    writer.startTag(HtmlElem.A);
    writer.attribute(HtmlAttr.CLASS, "dropdown-toggle");
    writer.attribute("data-toggle", "dropdown");
    writer.attribute(HtmlAttr.ROLE, "button");
    writer.attribute("aria-expanded", "false");
    writer.text(menu.getTitle());
    writer.text(" ");
    icon(writer, "caret");
    writer.endTag(HtmlElem.A);

    writer.startTag(HtmlElem.UL);
    writer.attribute(HtmlAttr.CLASS, "dropdown-menu");
    writer.attribute(HtmlAttr.ROLE, "menu");
  }

  public static void navbarEnd(final XmlWriter writer) {
    writer.endTagLn(HtmlElem.UL);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.NAV);
  }

  public static void navbarStart(final XmlWriter writer, final Navbar navbar,
    final JexlHttpServletRequestContext jexlContext) {

    writer.startTag(HtmlElem.NAV);

    final String id = navbar.getId();
    writer.attribute(HtmlAttr.ID, id);
    final String navbarCssClass = "navbar navbar-default " + navbar.getNavbarCssClass();
    writer.attribute(HtmlAttr.CLASS, navbarCssClass);
    writer.newLine();

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "container");
    writer.newLine();

    {
      writer.startTag(HtmlElem.DIV);
      writer.attribute(HtmlAttr.CLASS, "navbar-header");
      writer.newLine();
      {
        writer.startTag(HtmlElem.BUTTON);
        writer.attribute(HtmlAttr.TYPE, "button");
        writer.attribute(HtmlAttr.CLASS, "navbar-toggle collapsed");
        writer.attribute("data-toggle", "collapse");
        writer.attribute("data-target", "#" + id + "Bar");
        writer.attribute("aria-expanded", "false");
        writer.attribute("aria-controls", "navbar");
        writer.newLine();

        HtmlUtil.serializeSpan(writer, "sr-only", "Toggle navigation");

        for (int i = 0; i < 3; i++) {
          icon(writer, "icon-bar");
        }
        writer.endTagLn(HtmlElem.BUTTON);

        final List<Brand> brands = navbar.getBrands();
        navBarBrand(writer, brands);
        writer.endTagLn(HtmlElem.DIV);
      }
      {
        writer.startTag(HtmlElem.DIV);
        writer.attribute(HtmlAttr.ID, id + "Bar");
        writer.attribute(HtmlAttr.CLASS, "navbar-collapse collapse");
        writer.attribute("aria-expanded", "false");
        writer.newLine();

        writer.startTag(HtmlElem.UL);
        final String cssClass = "nav navbar-nav navbar-" + navbar.getNavbarAlign();
        writer.attribute(HtmlAttr.CLASS, cssClass);
      }
    }
  }
}
