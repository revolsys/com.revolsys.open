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

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;

/**
 * @author paustin
 * @version 1.0
 */
public class DivElementContainer extends ElementContainer {
  private String id;

  private String cssClass;

  private String style;

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

  /*
   * (non-Javadoc)
   * @see com.revolsys.ui.model.Element#serializeElement(com.revolsys.io.xml.
   * XmlWriter )
   */
  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    if (this.id != null) {
      out.attribute(HtmlUtil.ATTR_ID, this.id);
    }
    if (this.cssClass != null) {
      out.attribute(HtmlUtil.ATTR_CLASS, this.cssClass);
    }
    if (this.style != null) {
      out.attribute(HtmlUtil.ATTR_STYLE, this.style);
    }
    super.serializeElement(out);
    out.endTag(HtmlUtil.DIV);
  }

  public void setStyle(final String style) {
    this.style = style;
  }
}
