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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class UnorderedListLayout implements ElementContainerLayout {
  private String cssClass;

  public UnorderedListLayout() {
  }

  public UnorderedListLayout(final String cssClass) {
    this.cssClass = cssClass;
  }

  @Override
  public void serialize(final XmlWriter out, final ElementContainer container) {
    out.startTag(HtmlElem.UL);
    if (this.cssClass != null) {
      out.attribute(HtmlAttr.CLASS, this.cssClass);
    }
    for (final Object element2 : container.getElements()) {
      final Element element = (Element)element2;
      out.startTag(HtmlElem.LI);
      element.serialize(out);
      out.endTag(HtmlElem.LI);
    }
    out.endTag(HtmlElem.UL);
  }
}
