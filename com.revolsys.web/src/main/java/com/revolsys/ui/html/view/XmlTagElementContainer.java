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

import java.io.IOException;

import javax.xml.namespace.QName;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

/**
 * @author paustin
 * @version 1.0
 */
public class XmlTagElementContainer extends ElementContainer {
  private QName tagName;
  private String cssClass;

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
   * 
   * @see com.revolsys.ui.model.Element#serializeElement(com.revolsys.xml.io.XmlWriter)
   */
  public void serializeElement(final XmlWriter out) throws IOException {
    out.startTag(tagName);
    if (cssClass != null) {
      out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    }
    super.serializeElement(out);
    out.endTag(tagName);
  }
}
