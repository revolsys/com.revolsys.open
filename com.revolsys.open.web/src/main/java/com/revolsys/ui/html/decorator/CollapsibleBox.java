package com.revolsys.ui.html.decorator;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.view.Element;

public class CollapsibleBox implements Decorator {

  private final String title;

  private boolean open;

  public CollapsibleBox(final String title) {
    this.title = title;
  }

  public CollapsibleBox(final String title, final boolean open) {
    this.title = title;
    this.open = open;
  }

  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "collapsibleBox");

    out.startTag(HtmlUtil.DIV);
    if (open) {
      out.attribute(HtmlUtil.ATTR_CLASS, "open");
    } else {
      out.attribute(HtmlUtil.ATTR_CLASS, "closed");
    }

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "title");
    out.text(title);
    out.endTag(HtmlUtil.DIV);

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "content");
    element.serializeElement(out);
    out.endTag(HtmlUtil.DIV);

    out.endTag(HtmlUtil.DIV);
    out.endTag(HtmlUtil.DIV);
  }

}
