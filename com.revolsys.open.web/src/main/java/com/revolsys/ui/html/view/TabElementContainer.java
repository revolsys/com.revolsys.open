package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.layout.DivLayout;

public class TabElementContainer extends ElementContainer {

  private final String id = "tab-" + UUID.randomUUID();

  private final List<String> ids = new ArrayList<String>();

  private final List<String> labels = new ArrayList<String>();

  private Integer selectedIndex;

  public TabElementContainer() {
    setLayout(new DivLayout());
  }

  public void add(final String label, final Element element) {
    final String tabId = id + "-" + getElements().size();
    add(tabId, label, element);
  }

  public void add(final String tabId, final String label, final Element element) {
    add(element);
    labels.add(label);
    ids.add(tabId);
  }

  public Integer getSelectedIndex() {
    return selectedIndex;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "jqueryTabs");
    out.attribute(HtmlUtil.ATTR_ID, id);
    out.startTag(HtmlUtil.UL);
    int i = 0;
    for (final String label : labels) {
      final String id = ids.get(i++);
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
      final String id = ids.get(i++);
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_ID, id);
      element.serialize(out);
      out.endTag(HtmlUtil.DIV);
    }

    out.endTag(HtmlUtil.DIV);
    if (selectedIndex != null) {
      out.startTag(HtmlUtil.SCRIPT);
      out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
      out.text("$(document).ready(function() {$('#" + id
        + "').tabs('option', 'active', " + selectedIndex + ");});");
      out.endTag(HtmlUtil.SCRIPT);
    }
  }

  public void setSelectedIndex(final Integer selectedIndex) {
    this.selectedIndex = selectedIndex;
  }
}
