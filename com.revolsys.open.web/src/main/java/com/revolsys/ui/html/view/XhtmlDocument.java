package com.revolsys.ui.html.view;

import java.io.IOException;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class XhtmlDocument {
  private Element body;

  public Element getBody() {
    return this.body;
  }

  public void serialize(final XmlWriter out) throws IOException {
    out.startDocument("UTF-8", "1.0");
    out.docType("html", "-//W3C//DTD XHTML 1.1//EN",
      "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
    out.startTag(HtmlElem.HTML);
    out.attribute(HtmlAttr.LANG, "en");

    out.startTag(HtmlElem.HEAD);
    serializeScripts(out);
    serializeStyles(out);
    out.endTag(HtmlElem.HEAD);

    out.startTag(HtmlElem.BODY);
    serializeBody(out);
    out.endTag(HtmlElem.BODY);

    out.endTag(HtmlElem.HTML);
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
