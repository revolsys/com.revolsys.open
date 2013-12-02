package com.revolsys.ui.html.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.layout.ElementContainerLayout;
import com.revolsys.ui.html.layout.RawLayout;

public class ElementContainer extends Element {
  private final List<ElementContainer> containers = new ArrayList<ElementContainer>();

  private final List<Element> elements = new ArrayList<Element>();

  private final List<Element> elementsExternal = Collections.unmodifiableList(elements);

  private final Map<String, Field> fields = new HashMap<String, Field>();

  private ElementContainerLayout layout = new RawLayout();

  public ElementContainer() {
    this(new RawLayout());
  }

  public ElementContainer(final Element... elements) {
    add(elements);
  }

  public ElementContainer(Decorator decorator) {
    setDecorator(decorator);
  }

  public ElementContainer(final ElementContainerLayout layout) {
    this.layout = layout;
  }

  public ElementContainer(Decorator decorator, Element... elements) {
    setDecorator(decorator);
    add(elements);
  }

  public ElementContainer add(final Element element) {
    if (element != null) {
      elements.add(element);
      element.setContainer(this);
      if (element instanceof Field) {
        final Field field = (Field)element;
        fields.put(field.getName(), field);
      } else if (element instanceof ElementContainer) {
        final ElementContainer container = (ElementContainer)element;
        containers.add(container);
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
    elements.add(index, element);

  }

  public ElementContainer add(final String content) {
    return add(new StringElement(content));
  }

  public List<Element> getElements() {
    return elementsExternal;
  }

  public Field getField(final String name) {
    Field field = fields.get(name);
    if (field != null) {
      return field;
    }
    for (final ElementContainer container : containers) {
      field = container.getField(name);
      if (field != null) {
        return field;
      }
    }
    return null;
  }

  public List<String> getFieldNames() {
    final List<String> allFields = new ArrayList<String>();
    allFields.addAll(fields.keySet());
    for (final ElementContainer container : containers) {
      allFields.addAll(container.getFieldNames());
    }
    return allFields;
  }

  public Map<String, Field> getFields() {
    final Map<String, Field> allFields = new HashMap<String, Field>();
    allFields.putAll(fields);
    for (final ElementContainer container : containers) {
      allFields.putAll(container.getFields());
    }
    return allFields;
  }

  public <T> T getInitialValue(final Field field,
    final HttpServletRequest request) {
    return (T)getContainer().getInitialValue(field, request);
  }

  /**
   * @return Returns the layout.
   */
  public ElementContainerLayout getLayout() {
    return layout;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    for (final Element element : elements) {
      element.initialize(request);
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    layout.serialize(out, this);
  }

  /**
   * @param layout The layout to set.
   */
  public void setLayout(final ElementContainerLayout layout) {
    this.layout = layout;
  }

  public boolean validate() {
    boolean valid = true;
    for (final ElementContainer container : containers) {
      valid &= container.validate();
    }
    return valid;
  }
}
