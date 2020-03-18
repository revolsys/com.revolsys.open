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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class TableLayout implements ElementContainerLayout {
  private static final Logger log = LoggerFactory.getLogger(TableLayout.class);

  private final String cssClass;

  private final List cssClasses = new ArrayList();

  private final int numColumns;

  private final List titles;

  public TableLayout(final int numColumns) {
    this(null, numColumns);
  }

  public TableLayout(final String cssClass, final int numColumns) {
    this(cssClass, numColumns, null);
  }

  public TableLayout(final String cssClass, final int numColumns, final List titles) {
    this(cssClass, numColumns, titles, Collections.EMPTY_LIST);
  }

  public TableLayout(final String cssClass, final int numColumns, final List titles,
    final List cssClasses) {
    this.cssClass = cssClass;
    this.numColumns = numColumns;
    this.titles = titles;
    this.cssClasses.addAll(cssClasses);
    for (int i = cssClasses.size(); i < numColumns; i++) {
      this.cssClasses.add("");
    }
  }

  @Override
  public void serialize(final XmlWriter out, final ElementContainer container) {
    if (!container.getElements().isEmpty()) {
      out.startTag(HtmlElem.DIV);
      if (this.cssClass != null) {
        out.attribute(HtmlAttr.CLASS, this.cssClass);
      }
      out.startTag(HtmlElem.TABLE);
      out.attribute(HtmlAttr.CELL_SPACING, "0");
      out.attribute(HtmlAttr.CELL_PADDING, "0");

      serializeThead(out);

      serializeTbody(out, container);
      out.endTag(HtmlElem.TABLE);
      out.endTag(HtmlElem.DIV);
    }
  }

  private void serializeTbody(final XmlWriter out, final ElementContainer container) {
    out.startTag(HtmlElem.TBODY);
    final List elementList = container.getElements();
    int i = 0;
    int rowNum = 0;
    final int numElements = elementList.size();
    final int lastRow = (numElements - 1) / this.numColumns;
    for (final Iterator elements = elementList.iterator(); elements.hasNext();) {
      final Element element = (Element)elements.next();
      final int col = i % this.numColumns;
      String colCss = (String)this.cssClasses.get(col);
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

  private void serializeThead(final XmlWriter out) {
    if (this.titles != null && !this.titles.isEmpty()) {
      out.startTag(HtmlElem.THEAD);
      out.startTag(HtmlElem.TR);
      int col = 0;
      for (final Iterator titleIter = this.titles.iterator(); titleIter.hasNext();) {
        final String title = (String)titleIter.next();
        out.startTag(HtmlElem.TH);
        String colCssClass = (String)this.cssClasses.get(col);
        if (col == 0) {
          colCssClass += " firstCol";
        }
        if (col == this.numColumns) {
          colCssClass += " lastCol";
        }
        if (colCssClass.length() > 0) {
          out.attribute(HtmlAttr.CLASS, colCssClass);
        }
        out.text(title);
        out.endTag(HtmlElem.TH);
        col++;
      }
      out.endTag(HtmlElem.TR);
      out.endTag(HtmlElem.THEAD);
    }
  }
}
