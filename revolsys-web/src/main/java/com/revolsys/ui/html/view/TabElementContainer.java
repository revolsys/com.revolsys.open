package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.revolsys.record.io.format.html.Aria;
import com.revolsys.record.io.format.html.Data;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.layout.DivLayout;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class TabElementContainer extends ElementContainer {

  private final String id = "tab-" + UUID.randomUUID();

  private final List<String> ids = new ArrayList<>();

  private final List<String> labels = new ArrayList<>();

  private int selectedIndex = 0;

  public TabElementContainer() {
    setLayout(new DivLayout());
  }

  public void add(final String label, final Element element) {
    final String tabId = this.id + "-" + getElements().size();
    add(tabId, label, element);
  }

  public void add(final String tabId, final String label, final Element element) {
    add(element);
    this.labels.add(label);
    this.ids.add(tabId);
  }

  public Integer getSelectedIndex() {
    return this.selectedIndex;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.ROLE, "tabpanel");
    out.attribute(HtmlAttr.ID, this.id);

    out.startTag(HtmlElem.UL);
    out.attribute(HtmlAttr.CLASS, "nav nav-tabs");
    out.attribute(HtmlAttr.ROLE, "tablist");
    int i = 0;
    for (final String label : this.labels) {
      final String id = this.ids.get(i);
      out.startTag(HtmlElem.LI);
      out.attribute(HtmlAttr.ROLE, "presentation");
      if (this.selectedIndex == i) {
        out.attribute(HtmlAttr.CLASS, "active");
      }
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, "#" + id);
      Aria.controls(out, id);
      out.attribute(HtmlAttr.ROLE, "tab");
      Data.toggle(out, "tab");
      out.text(label);
      out.endTag(HtmlElem.A);
      out.endTag(HtmlElem.LI);
      i++;
    }
    out.endTag(HtmlElem.UL);

    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "tab-content");
    i = 0;
    for (final Element element : getElements()) {
      final String id = this.ids.get(i);
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.ROLE, "tabpanel");
      if (this.selectedIndex == i) {
        out.attribute(HtmlAttr.CLASS, "tab-pane active");
      } else {
        out.attribute(HtmlAttr.CLASS, "tab-pane");
      }
      out.attribute(HtmlAttr.ID, id);
      element.serialize(out);
      out.endTag(HtmlElem.DIV);
      i++;
    }
    out.endTag(HtmlElem.DIV);

    out.endTag(HtmlElem.DIV);

  }

  public void serializeElementOld(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "jqueryTabs");
    out.attribute(HtmlAttr.ID, this.id);
    out.startTag(HtmlElem.UL);
    int i = 0;
    for (final String label : this.labels) {
      final String id = this.ids.get(i++);
      out.startTag(HtmlElem.LI);
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, "#" + id);
      out.text(label);
      out.endTag(HtmlElem.A);
      out.endTag(HtmlElem.LI);
    }
    out.endTag(HtmlElem.UL);

    i = 0;
    for (final Element element : getElements()) {
      final String id = this.ids.get(i++);
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.ID, id);
      element.serialize(out);
      out.endTag(HtmlElem.DIV);
    }

    out.endTag(HtmlElem.DIV);
    if (this.selectedIndex != 0) {
      out.startTag(HtmlElem.SCRIPT);
      out.attribute(HtmlAttr.TYPE, "text/javascript");
      out.text("$(document).ready(function() {$('#" + this.id + "').tabs('option', 'active', "
        + this.selectedIndex + ");});");
      out.endTag(HtmlElem.SCRIPT);
    }
  }

  public void setSelectedIndex(final int selectedIndex) {
    this.selectedIndex = selectedIndex;
  }
}
