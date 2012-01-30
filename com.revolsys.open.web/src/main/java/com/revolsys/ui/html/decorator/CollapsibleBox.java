package com.revolsys.ui.html.decorator;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.view.Element;

public class CollapsibleBox implements Decorator {

  private String title;

  public CollapsibleBox(String title) {
    this.title = title;
  }

  public void serialize(XmlWriter out, Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "collapsibleBox");

    out.startTag(HtmlUtil.DIV);

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
