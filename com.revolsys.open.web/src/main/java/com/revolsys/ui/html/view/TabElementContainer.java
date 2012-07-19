package com.revolsys.ui.html.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.layout.DivLayout;

public class TabElementContainer extends ElementContainer {

  private String id = "tab-" + UUID.randomUUID();

  public TabElementContainer() {
    setLayout(new DivLayout());
  }

  private Map<String, Element> elementMap = new LinkedHashMap<String, Element>();

  @Override
  public void serializeElement(XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "jqueryTabs");
    out.attribute(HtmlUtil.ATTR_ID, id);
    out.startTag(HtmlUtil.UL);
    int i = 0;
    for (final String label : elementMap.keySet()) {
      out.startTag(HtmlUtil.LI);
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, "#" + id + "-" + i++);
      out.text(label);
      out.endTag(HtmlUtil.A);
      out.endTag(HtmlUtil.LI);
    }
    out.endTag(HtmlUtil.UL);

    i = 0;
    for (final Element element : getElements()) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_ID, id + "-" + i++);
      element.serialize(out);
      out.endTag(HtmlUtil.DIV);
    }

    out.endTag(HtmlUtil.DIV);
  }

  public void add(String label, Element element) {
    add(element);
    elementMap.put(label, element);
  }
}
