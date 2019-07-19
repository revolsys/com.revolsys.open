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
package com.revolsys.ui.html.layout;

import java.util.Iterator;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class DefinitionListLayout implements ElementContainerLayout {
  private String cssClass;

  public DefinitionListLayout() {
  }

  public DefinitionListLayout(final String cssClass) {
    this.cssClass = cssClass;
  }

  @Override
  public void serialize(final XmlWriter out, final ElementContainer container) {
    out.startTag(HtmlElem.DL);
    if (this.cssClass != null) {
      out.attribute(HtmlAttr.CLASS, this.cssClass);
    }
    for (final Iterator elements = container.getElements().iterator(); elements.hasNext();) {
      Element element = (Element)elements.next();
      out.startTag(HtmlElem.DT);
      element.serialize(out);
      out.endTag(HtmlElem.DT);
      out.startTag(HtmlElem.DD);
      if (elements.hasNext()) {
        element = (Element)elements.next();
        element.serialize(out);
      } else {
        out.entityRef("nbsp");
      }
      out.endTag(HtmlElem.DD);
    }
    out.endTag(HtmlElem.DL);
  }
}
