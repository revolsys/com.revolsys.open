package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.layout.DivLayout;

public class TabElementContainer extends ElementContainer {

  private String id = "tab-" + UUID.randomUUID();

  public TabElementContainer() {
    setLayout(new DivLayout());
  }

  private List<String> ids = new ArrayList<String>();

  private List<String> labels = new ArrayList<String>();

  @Override
  public void serializeElement(XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "jqueryTabs");
    out.attribute(HtmlUtil.ATTR_ID, id);
    out.startTag(HtmlUtil.UL);
    int i = 0;
    for (final String label : labels) {
      String id = ids.get(i++);
      out.startTag(HtmlUtil.LI);
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, "#" + id);
      out.text(label);
      out.endTag(HtmlUtil.A);
      out.endTag(HtmlUtil.LI);
    }
    out.endTag(HtmlUtil.UL);

    i = 0;
    for (final Element element : getElements()) {
      String id = ids.get(i++);
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_ID, id);
      element.serialize(out);
      out.endTag(HtmlUtil.DIV);
    }

    out.endTag(HtmlUtil.DIV);
  }

  public void add(String label, Element element) {
    String tabId = id + "-" + getElements().size();
    add(tabId, label, element);
  }

  public void add(String tabId, String label, Element element) {
    add(element);
    labels.add(label);
    ids.add(tabId);
  }
}
