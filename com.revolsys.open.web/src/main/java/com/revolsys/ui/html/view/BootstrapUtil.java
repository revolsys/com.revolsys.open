package com.revolsys.ui.html.view;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.model.Menu;
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

  public static void navbarStart(final XmlWriter writer, final String id, final String navbarClass,
    final String navMenuClass, final String title, String uri) {
    writer.startTag(HtmlUtil.NAV);
    writer.attribute(HtmlUtil.ATTR_ID, id);
    writer.attribute(HtmlUtil.ATTR_CLASS, "navbar navbar-default " + navbarClass);
    writer.newLine();

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
      }
      if (Property.hasValue(title)) {
        writer.startTag(HtmlUtil.A);
        writer.attribute(HtmlUtil.ATTR_CLASS, "navbar-brand");
        writer.attribute(HtmlUtil.ATTR_HREF, uri);
        writer.text(title);
        writer.endTag(HtmlUtil.A);
        writer.endTagLn(HtmlUtil.DIV);
      }
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
