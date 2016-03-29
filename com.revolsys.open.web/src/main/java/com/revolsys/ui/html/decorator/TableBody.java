package com.revolsys.ui.html.decorator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class TableBody implements Decorator {

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "objectView");
    out.startTag(HtmlElem.TABLE);
    out.attribute(HtmlAttr.CLASS, "data");
    out.startTag(HtmlElem.TBODY);
    element.serializeElement(out);
    out.endTag(HtmlElem.TBODY);
    out.endTag(HtmlElem.TABLE);
    out.endTag(HtmlElem.DIV);
  }
}
