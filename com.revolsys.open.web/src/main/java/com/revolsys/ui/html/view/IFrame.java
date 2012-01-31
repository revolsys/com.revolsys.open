package com.revolsys.ui.html.view;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.decorator.Decorator;

public class IFrame extends Element {
  private String src;

  private String cssClass;

  public IFrame(String src) {
    this.src = src;
  }

  public IFrame(String src, Decorator decorator) {
    this.src = src;
    setDecorator(decorator);
  }

  public IFrame(String src, String cssClass, Decorator decorator) {
    this.src = src;
    this.cssClass = cssClass;
    setDecorator(decorator);
  }

  @Override
  public void serializeElement(XmlWriter out) {
    out.startTag(HtmlUtil.IFRAME);
    out.attribute(HtmlUtil.ATTR_SRC, src);
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    out.startTag(HtmlUtil.A);
    out.attribute(HtmlUtil.ATTR_HREF, src);
    out.text(src);
    out.endTag();
    out.endTag(HtmlUtil.IFRAME);
  }
}
