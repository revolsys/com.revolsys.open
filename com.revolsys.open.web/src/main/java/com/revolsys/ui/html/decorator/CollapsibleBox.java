package com.revolsys.ui.html.decorator;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.view.Element;

public class CollapsibleBox implements Decorator {

  private String title;

  private boolean open;

  public CollapsibleBox() {
  }

  public CollapsibleBox(final String title) {
    this.title = title;
  }

  public CollapsibleBox(final String title, final boolean open) {
    this.title = title;
    this.open = open;
  }

  public String getTitle() {
    return title;
  }

  public boolean isOpen() {
    return open;
  }

  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    String cssClass = "collapsibleBox";
    if (!open) {
      cssClass += " closed";
    }
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);

    out.startTag(HtmlUtil.H3);
    HtmlUtil.serializeA(out, null, "#", title);
    out.endTag(HtmlUtil.H3);

    out.startTag(HtmlUtil.DIV);
    element.serializeElement(out);
    out.endTag(HtmlUtil.DIV);

    out.endTag(HtmlUtil.DIV);
  }

  public void setOpen(final boolean open) {
    this.open = open;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
