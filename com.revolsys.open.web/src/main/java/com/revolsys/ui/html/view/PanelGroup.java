package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.html.Aria;
import com.revolsys.record.io.format.html.Data;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class PanelGroup extends ElementContainer {
  private static AtomicInteger ID_GEN = new AtomicInteger();

  private final Map<String, ElementContainer> containers = new LinkedHashMap<>();

  private String id;

  private boolean multipleSelect = true;

  private final Map<String, Boolean> states = new HashMap<>();

  private final Map<String, String> titles = new HashMap<>();

  public PanelGroup() {
    this("panel" + ID_GEN.incrementAndGet());
  }

  public PanelGroup(final String id) {
    this.id = id;
  }

  public void addElement(final String panelId, final Element element) {
    final ElementContainer container = getPanel(panelId);
    container.add(element);
  }

  public void addElement(final String panelId, final Element element, final Decorator decorator) {
    final ElementContainer container = getPanel(panelId);
    container.add(element, decorator);
  }

  public void addPanel(final String panelId, final String title) {
    getPanel(panelId);
    this.titles.put(panelId, title);
  }

  @Override
  public List<Element> getElements() {
    return new ArrayList<>(this.containers.values());
  }

  @Override
  public Field getField(final String name) {
    for (final ElementContainer container : this.containers.values()) {
      final Field field = container.getField(name);
      if (field != null) {
        return field;
      }
    }
    return null;
  }

  @Override
  public List<String> getFieldNames() {
    final List<String> allFields = new ArrayList<>();
    for (final ElementContainer container : this.containers.values()) {
      allFields.addAll(container.getFieldNames());
    }
    return allFields;
  }

  @Override
  public Map<String, Field> getFields() {
    final Map<String, Field> allFields = new HashMap<>();
    for (final ElementContainer container : this.containers.values()) {
      allFields.putAll(container.getFields());
    }
    return allFields;
  }

  public String getId() {
    return this.id;
  }

  private ElementContainer getPanel(final String panelId) {
    ElementContainer container = this.containers.get(panelId);
    if (container == null) {
      container = new ElementContainer();
      container.setContainer(this);
      this.containers.put(panelId, container);
      this.titles.put(panelId, panelId);
      this.states.put(panelId, false);
    }
    return container;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    for (final Element element : this.containers.values()) {
      element.initialize(request);
    }
  }

  @Override
  public boolean isEmpty() {
    return this.containers.size() == 0;
  }

  public boolean isMultipleSelect() {
    return this.multipleSelect;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (!this.containers.isEmpty()) {
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.ID, this.id);
      out.attribute(HtmlAttr.ROLE, "tablist");
      out.attribute("aria-multiselectable", this.multipleSelect);

      for (final Entry<String, ElementContainer> entry : this.containers.entrySet()) {
        final String panelId = entry.getKey();
        final ElementContainer container = entry.getValue();
        if (!container.isEmpty()) {
          final String title = this.titles.get(panelId);
          final boolean open = this.states.get(panelId);
          final String fullPanelId = this.id + "_" + panelId;
          out.startTag(HtmlElem.DIV);
          out.attribute(HtmlAttr.CLASS, "panel panel-default panel-collapsible");
          final String headingId = "heading" + fullPanelId;
          final String collapseId = "collapse" + fullPanelId;
          {
            out.startTag(HtmlElem.DIV);
            out.attribute(HtmlAttr.CLASS, "panel-heading");
            out.attribute(HtmlAttr.ROLE, "tab");
            out.attribute(HtmlAttr.ID, headingId);
            {
              out.startTag(HtmlElem.H4);
              out.attribute(HtmlAttr.CLASS, "panel-title");
              {
                out.startTag(HtmlElem.A);
                if (!open) {
                  out.attribute(HtmlAttr.CLASS, "collapsed");
                }
                out.attribute(HtmlAttr.ROLE, "button");
                Data.toggle(out, "collapse");
                Data.parent(out, "#" + this.id);
                out.attribute(HtmlAttr.HREF, "#collapse" + fullPanelId);
                Aria.expanded(out, open);
                Aria.controls(out, collapseId);
                out.text(title);
                out.endTag(HtmlElem.A);
              }
              out.endTag(HtmlElem.H4);
            }
            out.endTag(HtmlElem.DIV);
          }
          {
            out.startTag(HtmlElem.DIV);
            out.attribute(HtmlAttr.ID, collapseId);
            if (open) {
              out.attribute(HtmlAttr.CLASS, "panel-collapse collapse in");
            } else {
              out.attribute(HtmlAttr.CLASS, "panel-collapse collapse");
            }
            out.attribute(HtmlAttr.ROLE, "tabpanel");
            Aria.labelledby(out, headingId);
            {
              out.startTag(HtmlElem.DIV);
              out.attribute(HtmlAttr.CLASS, "panel-body");
              container.serialize(out);
              out.endTag(HtmlElem.DIV);
            }
            out.endTag(HtmlElem.DIV);
          }
          out.endTag(HtmlElem.DIV);
        }
      }
      out.endTag(HtmlElem.DIV);
    }
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setMultipleSelect(final boolean multipleSelect) {
    this.multipleSelect = multipleSelect;
  }

  public void setOpen(final String id, final boolean open) {
    this.states.put(id, open);
  }

  @Override
  public boolean validate() {
    boolean valid = true;
    for (final ElementContainer container : this.containers.values()) {
      valid &= container.validate();
    }
    return valid;
  }
}
