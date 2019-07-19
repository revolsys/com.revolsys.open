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
package com.revolsys.ui.web.config;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.view.Element;

public class ElementComponent extends Component {
  private static final Logger log = LoggerFactory.getLogger(ElementComponent.class);

  private String attribute;

  public ElementComponent(final ElementComponent component) {
    super(component);
    setAttribute(component.attribute);
  }

  public ElementComponent(final String area, final String name, final String attribute) {
    super(area, name);
    setAttribute(attribute);
  }

  @Override
  public Object clone() {
    return new ElementComponent(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof ElementComponent) {
      final ElementComponent c = (ElementComponent)o;
      if (equalsWithNull(c.attribute, this.attribute) && super.equals(o)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate the hash code for the object.
   *
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return super.hashCode() + (this.attribute.hashCode() << 2);
  }

  @Override
  public void includeComponent(final PageContext context) throws ServletException, IOException {
    final Object object = context.findAttribute(this.attribute);
    if (object instanceof Element) {
      final Element element = (Element)object;
      final Writer out = context.getOut();
      element.serialize(out);

    }
  }

  private void setAttribute(final String attribute) {
    this.attribute = attribute;
  }
}
