package com.revolsys.ui.html.view;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.JexlHttpServletRequestContext;
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

  private static void navBarBrand(final XmlWriter writer, final String title, final String imageSrc,
    final String uri) {
    writer.startTag(HtmlElem.A);
    writer.attribute(HtmlAttr.CLASS, "navbar-brand");
    writer.attribute(HtmlAttr.HREF, uri);
    if (Property.hasValue(imageSrc)) {
      writer.attribute(HtmlAttr.TITLE, title);
      HtmlUtil.serializeImage(writer, imageSrc, title);
    } else {
      writer.text(title);
    }
    writer.endTag(HtmlElem.A);
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

  public static void navbarStart(final XmlWriter writer, final String navbarClass,
    final String navMenuClass, final Menu menu, final JexlHttpServletRequestContext jexlContext) {

    writer.startTag(HtmlElem.NAV);

    final String id = menu.getId();
    writer.attribute(HtmlAttr.ID, id);
    writer.attribute(HtmlAttr.CLASS, "navbar navbar-default " + navbarClass);
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

        final String title = menu.getTitle();
        final String imageSrc = menu.getImageSrc();
        final String uri = menu.getLink(jexlContext);
        if (Property.hasValuesAny(imageSrc, title)) {
          navBarBrand(writer, title, imageSrc, uri);
        }
        writer.endTagLn(HtmlElem.DIV);
      }
      {
        writer.startTag(HtmlElem.DIV);
        writer.attribute(HtmlAttr.ID, id + "Bar");
        writer.attribute(HtmlAttr.CLASS, "navbar-collapse collapse");
        writer.attribute("aria-expanded", "false");
        writer.newLine();

        writer.startTag(HtmlElem.UL);
        writer.attribute(HtmlAttr.CLASS, "nav navbar-nav " + navMenuClass);

      }
    }

  }
}
