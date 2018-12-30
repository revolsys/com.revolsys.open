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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class TableBodyLayout implements ElementContainerLayout {
  private static final Logger log = LoggerFactory.getLogger(TableLayout.class);

  private final String cssClass;

  private final List<String> cssClasses = new ArrayList<>();

  private final int numColumns;

  public TableBodyLayout(final int numColumns) {
    this(null, numColumns);
  }

  public TableBodyLayout(final String cssClass, final int numColumns, final String... cssClasses) {
    this.cssClass = cssClass;
    this.numColumns = numColumns;
    for (final String colCss : cssClasses) {
      this.cssClasses.add(colCss);
    }
    for (int i = cssClasses.length; i < numColumns; i++) {
      this.cssClasses.add("");
    }
  }

  @Override
  public void serialize(final XmlWriter out, final ElementContainer container) {
    if (!container.getElements().isEmpty()) {
      serializeTbody(out, container);
    }
  }

  private void serializeTbody(final XmlWriter out, final ElementContainer container) {
    out.startTag(HtmlElem.TBODY);
    if (this.cssClass != null) {
      out.attribute(HtmlAttr.CLASS, this.cssClass);
    }
    final List<Element> elementList = container.getElements();
    int i = 0;
    int rowNum = 0;
    final int numElements = elementList.size();
    final int lastRow = (numElements - 1) / this.numColumns;
    for (final Element element : elementList) {
      final int col = i % this.numColumns;
      String colCss = this.cssClasses.get(col);
      final boolean firstCol = col == 0;
      final boolean lastCol = (i + 1) % this.numColumns == 0 || i == numElements - 1;
      if (firstCol) {
        out.startTag(HtmlElem.TR);
        String rowCss = "";
        if (rowNum == 0) {
          rowCss += " firstRow";
        }
        if (rowNum == lastRow) {
          rowCss += " lastRow";
        }
        if (rowCss.length() > 0) {
          out.attribute(HtmlAttr.CLASS, rowCss);
        }
        colCss += " firstCol";
      }
      if (lastCol) {
        colCss += " lastCol";
      }
      out.startTag(HtmlElem.TD);
      if (colCss.length() > 0) {
        out.attribute(HtmlAttr.CLASS, colCss);
      }
      element.serialize(out);
      out.endTag(HtmlElem.TD);
      i++;
      if (lastCol) {
        out.endTag(HtmlElem.TR);
        rowNum++;
      }
    }
    out.endTag(HtmlElem.TBODY);
  }
}
