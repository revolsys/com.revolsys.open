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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

/**
 * @author paustin
 * @version 1.0
 */
public class DivElementContainer extends ElementContainer {
  private String cssClass;

  private String id;

  private String style;

  private String role;

  public DivElementContainer() {
  }

  public DivElementContainer(final String cssClass) {
    this(null, cssClass);
  }

  public DivElementContainer(final String cssClass, final Element element) {
    this(cssClass);
    this.add(element);
  }

  public DivElementContainer(final String id, final String cssClass) {
    this.id = id;
    this.cssClass = cssClass;
  }

  public DivElementContainer(final String id, final String cssClass, final Element element) {
    this(id, cssClass);
    this.add(element);
  }

  public String getStyle() {
    return this.style;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.DIV);
    if (this.id != null) {
      out.attribute(HtmlAttr.ID, this.id);
    }
    if (this.cssClass != null) {
      out.attribute(HtmlAttr.CLASS, this.cssClass);
    }
    if (this.style != null) {
      out.attribute(HtmlAttr.STYLE, this.style);
    }
    if (this.role != null) {
      out.attribute(HtmlAttr.ROLE, this.role);
    }
    super.serializeElement(out);
    out.endTag(HtmlElem.DIV);
  }

  public DivElementContainer setRole(final String role) {
    this.role = role;
    return this;
  }

  public DivElementContainer setStyle(final String style) {
    this.style = style;
    return this;
  }
}
