package com.revolsys.ui.html.decorator;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.util.HtmlUtil;

public class CollapsibleBox implements Decorator {

  private boolean open;

  private String style;

  private String title;

  public CollapsibleBox() {
  }

  public CollapsibleBox(final String title) {
    this.title = title;
  }

  public CollapsibleBox(final String title, final boolean open) {
    this.title = title;
    this.open = open;
  }

  public CollapsibleBox(final String title, final String style, final boolean open) {
    this.title = title;
    this.style = style;
    this.open = open;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isOpen() {
    return this.open;
  }

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    String cssClass = "collapsibleBox";
    if (!this.open) {
      cssClass += " closed";
    }
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    out.attribute(HtmlUtil.ATTR_STYLE, this.style);

    out.startTag(HtmlUtil.H3);
    HtmlUtil.serializeA(out, null, "#", this.title);
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
