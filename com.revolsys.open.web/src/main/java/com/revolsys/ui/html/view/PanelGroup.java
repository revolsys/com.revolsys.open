package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.format.html.Aria;
import com.revolsys.format.html.Data;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.util.HtmlUtil;

public class PanelGroup extends ElementContainer {
  private static AtomicInteger ID_GEN = new AtomicInteger();

  private final Map<String, ElementContainer> containers = new LinkedHashMap<>();

  private final Map<String, String> titles = new HashMap<>();

  private String id;

  private boolean multipleSelect = true;

  private final Map<String, Boolean> states = new HashMap<>();

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
    final List<String> allFields = new ArrayList<String>();
    for (final ElementContainer container : this.containers.values()) {
      allFields.addAll(container.getFieldNames());
    }
    return allFields;
  }

  @Override
  public Map<String, Field> getFields() {
    final Map<String, Field> allFields = new HashMap<String, Field>();
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
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_ID, this.id);
      out.attribute(HtmlUtil.ATTR_ROLE, "tablist");
      out.attribute("aria-multiselectable", this.multipleSelect);

      for (final Entry<String, ElementContainer> entry : this.containers.entrySet()) {
        final String panelId = entry.getKey();
        final ElementContainer container = entry.getValue();
        if (!container.isEmpty()) {
          final String title = this.titles.get(panelId);
          final boolean open = this.states.get(panelId);
          final String fullPanelId = this.id + "_" + panelId;
          out.startTag(HtmlUtil.DIV);
          out.attribute(HtmlUtil.ATTR_CLASS, "panel panel-default");
          final String headingId = "heading" + fullPanelId;
          {
            out.startTag(HtmlUtil.DIV);
            out.attribute(HtmlUtil.ATTR_CLASS, "panel-heading");
            out.attribute(HtmlUtil.ATTR_ROLE, "tab");
            out.attribute(HtmlUtil.ATTR_ID, headingId);
            {
              out.startTag(HtmlUtil.H4);
              out.attribute(HtmlUtil.ATTR_CLASS, "panel-title");
              {
                out.startTag(HtmlUtil.A);
                Data.toggle(out, "collapse");
                out.attribute(HtmlUtil.ATTR_HREF, "#collapse" + fullPanelId);
                Aria.expanded(out, true);
                Aria.controls(out, "collapse" + fullPanelId);
                out.text(title);
                out.endTag(HtmlUtil.A);
              }
              out.endTag(HtmlUtil.H4);
            }
            out.endTag(HtmlUtil.DIV);
          }
          {
            out.startTag(HtmlUtil.DIV);
            out.attribute(HtmlUtil.ATTR_ID, "collapse" + fullPanelId);
            if (open) {
              out.attribute(HtmlUtil.ATTR_CLASS, "panel-collapse collapse in");
            } else {
              out.attribute(HtmlUtil.ATTR_CLASS, "panel-collapse collapse ");
            }
            out.attribute(HtmlUtil.ATTR_ROLE, "tabpanel");
            Aria.labelledby(out, headingId);
            {
              out.startTag(HtmlUtil.DIV);
              out.attribute(HtmlUtil.ATTR_CLASS, "panel-body");
              container.serialize(out);
              out.endTag(HtmlUtil.DIV);
            }
            out.endTag(HtmlUtil.DIV);
          }
          out.endTag(HtmlUtil.DIV);
        }
      }
      out.endTag(HtmlUtil.DIV);
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
