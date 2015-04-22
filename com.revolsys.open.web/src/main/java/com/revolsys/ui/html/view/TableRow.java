package com.revolsys.ui.html.view;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;

public class TableRow extends ElementContainer {
  public TableRow() {
  }

  public TableRow(final Element... elements) {
    super(elements);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.TR);
    super.serializeElement(out);
    out.endTag(HtmlUtil.TR);
  }
}
