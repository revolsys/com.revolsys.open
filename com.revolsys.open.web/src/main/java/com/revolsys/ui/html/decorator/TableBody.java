package com.revolsys.ui.html.decorator;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.ui.html.view.Element;

public class TableBody implements Decorator {

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "objectView");
    out.startTag(HtmlUtil.TABLE);
    out.attribute(HtmlUtil.ATTR_CLASS, "data");
    out.startTag(HtmlUtil.TBODY);
    element.serializeElement(out);
    out.endTag(HtmlUtil.TBODY);
    out.endTag(HtmlUtil.TABLE);
    out.endTag(HtmlUtil.DIV);
  }
}
