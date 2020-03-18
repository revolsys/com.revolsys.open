package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.layout.ElementContainerLayout;
import com.revolsys.ui.html.layout.RawLayout;

public class ElementContainer extends Element {
  private final List<ElementContainer> containers = new ArrayList<>();

  private final List<Element> elements = new ArrayList<>();

  private final List<Element> elementsExternal = Collections.unmodifiableList(this.elements);

  private final Map<String, Field> fields = new HashMap<>();

  private ElementContainerLayout layout = new RawLayout();

  public ElementContainer() {
    this(new RawLayout());
  }

  public ElementContainer(final Decorator decorator) {
    setDecorator(decorator);
  }

  public ElementContainer(final Decorator decorator, final Element... elements) {
    setDecorator(decorator);
    add(elements);
  }

  public ElementContainer(final Element... elements) {
    add(elements);
  }

  public ElementContainer(final ElementContainerLayout layout) {
    this.layout = layout;
  }

  public ElementContainer add(final Element element) {
    if (element != null) {
      this.elements.add(element);
      element.setContainer(this);
      if (element instanceof Field) {
        final Field field = (Field)element;
        this.fields.put(field.getName(), field);
      } else if (element instanceof ElementContainer) {
        final ElementContainer container = (ElementContainer)element;
        this.containers.add(container);
      }
    }
    return this;
  }

  public void add(final Element... elements) {
    for (final Element element : elements) {
      add(element);
    }
  }

  public ElementContainer add(final Element element, final Decorator decorator) {
    if (element != null) {
      element.setDecorator(decorator);
      return add(element);
    }
    return this;
  }

  public void add(final int index, final Element element) {
    this.elements.add(index, element);

  }

  public ElementContainer add(final String content) {
    return add(new StringElement(content));
  }

  public List<Element> getElements() {
    return this.elementsExternal;
  }

  public Field getField(final String name) {
    Field field = this.fields.get(name);
    if (field != null) {
      return field;
    }
    for (final ElementContainer container : this.containers) {
      field = container.getField(name);
      if (field != null) {
        return field;
      }
    }
    return null;
  }

  public List<String> getFieldNames() {
    final List<String> allFields = new ArrayList<>();
    allFields.addAll(this.fields.keySet());
    for (final ElementContainer container : this.containers) {
      allFields.addAll(container.getFieldNames());
    }
    return allFields;
  }

  public Map<String, Field> getFields() {
    final Map<String, Field> allFields = new HashMap<>();
    allFields.putAll(this.fields);
    for (final ElementContainer container : this.containers) {
      allFields.putAll(container.getFields());
    }
    return allFields;
  }

  public <T> T getInitialValue(final Field field, final HttpServletRequest request) {
    return (T)getContainer().getInitialValue(field, request);
  }

  /**
   * @return Returns the layout.
   */
  public ElementContainerLayout getLayout() {
    return this.layout;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    for (final Element element : this.elements) {
      element.initialize(request);
    }
  }

  public boolean isEmpty() {
    return this.elements.size() == 0;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    this.layout.serialize(out, this);
  }

  /**
   * @param layout The layout to set.
   */
  public void setLayout(final ElementContainerLayout layout) {
    this.layout = layout;
  }

  public boolean validate() {
    boolean valid = true;
    for (final ElementContainer container : this.containers) {
      valid &= container.validate();
    }
    return valid;
  }
}
