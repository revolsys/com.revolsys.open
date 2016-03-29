package com.revolsys.ui.html.decorator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
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
    out.startTag(HtmlElem.DIV);
    String cssClass = "collapsibleBox";
    if (!this.open) {
      cssClass += " closed";
    }
    out.attribute(HtmlAttr.CLASS, cssClass);
    out.attribute(HtmlAttr.STYLE, this.style);

    out.startTag(HtmlElem.H3);
    HtmlUtil.serializeA(out, null, "#", this.title);
    out.endTag(HtmlElem.H3);

    out.startTag(HtmlElem.DIV);
    element.serializeElement(out);
    out.endTag(HtmlElem.DIV);

    out.endTag(HtmlElem.DIV);
  }

  public void setOpen(final boolean open) {
    this.open = open;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
