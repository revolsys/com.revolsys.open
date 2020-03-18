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

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;

/**
 * @author paustin
 * @version 1.0
 */
public class XmlTagElementContainer extends ElementContainer {
  private final String cssClass;

  private final QName tagName;

  public XmlTagElementContainer(final QName tagName) {
    this(tagName, null, null);
  }

  public XmlTagElementContainer(final QName tagName, final String cssClass) {
    this(tagName, cssClass, null);
  }

  public XmlTagElementContainer(final QName tagName, final String cssClass, final Element element) {
    this.tagName = tagName;
    this.cssClass = cssClass;
    this.add(element);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.ui.model.Element#serializeElement(com.revolsys.io.xml.
   * XmlWriter )
   */
  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(this.tagName);
    if (this.cssClass != null) {
      out.attribute(HtmlAttr.CLASS, this.cssClass);
    }
    super.serializeElement(out);
    out.endTag(this.tagName);
  }
}
