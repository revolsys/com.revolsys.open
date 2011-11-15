/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  private List<ElementContainer> containers = new ArrayList<ElementContainer>();

  private List<Element> elements = new ArrayList<Element>();

  private List<Element> elementsExternal = Collections.unmodifiableList(elements);

  private Map<String, Field> fields = new HashMap<String, Field>();

  private ElementContainerLayout layout = new RawLayout();

  public ElementContainer() {
    this(new RawLayout());
  }

  public ElementContainer(final Element... elements) {
    add(elements);
  }

  public ElementContainer(final ElementContainerLayout layout) {
    this.layout = layout;
  }

  public ElementContainer add(final Element element) {
    if (element != null) {
      elements.add(element);
      element.setContainer(this);
      if (element instanceof Field) {
        Field field = (Field)element;
        fields.put(field.getName(), field);
      } else if (element instanceof ElementContainer) {
        ElementContainer container = (ElementContainer)element;
        containers.add(container);
      }
    }
    return this;
  }

  public void add(Element... elements) {
    for (Element element : elements) {
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

  public void add(int index, Element element) {
    elements.add(index, element);

  }

  public ElementContainer add(final String content) {
    return add(new StringElement(content));
  }

  public List<Element> getElements() {
    return elementsExternal;
  }

  public Field getField(final String name) {
    Field field = (Field)fields.get(name);
    if (field != null) {
      return field;
    }
    for (ElementContainer container : containers) {
      field = container.getField(name);
      if (field != null) {
        return field;
      }
    }
    return null;
  }

  public List<String> getFieldNames() {
    List<String> allFields = new ArrayList<String>();
    allFields.addAll(fields.keySet());
    for (ElementContainer container : containers) {
      allFields.addAll(container.getFieldNames());
    }
    return allFields;
  }

  public Map<String, Field> getFields() {
    Map<String, Field> allFields = new HashMap<String, Field>();
    allFields.putAll(fields);
    for (ElementContainer container : containers) {
      allFields.putAll(container.getFields());
    }
    return allFields;
  }

  public <T> T getInitialValue(final Field field, HttpServletRequest request) {
    return (T)getContainer().getInitialValue(field, request);
  }

  /**
   * @return Returns the layout.
   */
  public ElementContainerLayout getLayout() {
    return layout;
  }

  public void initialize(final HttpServletRequest request) {
    for (Element element : elements) {
      element.initialize(request);
    }
  }

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
    for (ElementContainer container : containers) {
      valid &= container.validate();
    }
    return valid;
  }
}
