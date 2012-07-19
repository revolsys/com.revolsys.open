package com.revolsys.ui.html.view;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.decorator.Decorator;

public class IFrame extends Element {
  private final String src;

  private String cssClass;

  private String style;

  public IFrame(final String src) {
    this.src = src;
  }

  public IFrame(final String src, final Decorator decorator) {
    this.src = src;
    setDecorator(decorator);
  }

  public IFrame(final String src, final String cssClass) {
    this.src = src;
    this.cssClass = cssClass;
  }

  public IFrame(final String src, final String cssClass,
    final Decorator decorator) {
    this.src = src;
    this.cssClass = cssClass;
    setDecorator(decorator);
  }

  public IFrame(final String src, final String cssClass, final String style,
    final Decorator decorator) {
    this.src = src;
    this.cssClass = cssClass;
    this.style = style;
    setDecorator(decorator);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.IFRAME);
    out.attribute(HtmlUtil.ATTR_SRC, src);
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    out.attribute(HtmlUtil.ATTR_STYLE, style);
    out.startTag(HtmlUtil.A);
    out.attribute(HtmlUtil.ATTR_HREF, src);
    out.text(src);
    out.endTag();
    out.endTag(HtmlUtil.IFRAME);
  }
}
