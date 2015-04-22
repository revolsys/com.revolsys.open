package com.revolsys.ui.html.view;

import java.io.IOException;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;

public class XhtmlDocument {
  private Element body;

  public Element getBody() {
    return this.body;
  }

  public void serialize(final XmlWriter out) throws IOException {
    out.startDocument("UTF-8", "1.0");
    out.docType("html", "-//W3C//DTD XHTML 1.1//EN",
        "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
    out.startTag(HtmlUtil.HTML);
    out.attribute(HtmlUtil.ATTR_LANG, "en");

    out.startTag(HtmlUtil.HEAD);
    serializeScripts(out);
    serializeStyles(out);
    out.endTag(HtmlUtil.HEAD);

    out.startTag(HtmlUtil.BODY);
    serializeBody(out);
    out.endTag(HtmlUtil.BODY);

    out.endTag(HtmlUtil.HTML);
    out.endDocument();
  }

  private void serializeBody(final XmlWriter out) {
  }

  private void serializeScripts(final XmlWriter out) {
  }

  private void serializeStyles(final XmlWriter out) {
  }

  public void setBody(final Element body) {
    this.body = body;
  }

}
