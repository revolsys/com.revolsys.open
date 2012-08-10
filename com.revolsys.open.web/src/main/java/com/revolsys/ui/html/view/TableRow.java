package com.revolsys.ui.html.view;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;

public class TableRow extends ElementContainer {
  public TableRow() {
  }

  public TableRow(Element... elements) {
    super(elements);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.TR);
    super.serializeElement(out);
    out.endTag(HtmlUtil.TR);
  }
}
