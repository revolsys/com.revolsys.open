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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.xml.io.XmlWriter;

public class TableLayout implements ElementContainerLayout {
  private static final Logger log = Logger.getLogger(TableLayout.class);

  private String cssClass;

  private int numColumns;

  private List titles;

  private List cssClasses = new ArrayList();

  public TableLayout(final int numColumns) {
    this(null, numColumns);
  }

  public TableLayout(final String cssClass, final int numColumns) {
    this(cssClass, numColumns, null);
  }

  public TableLayout(final String cssClass, final int numColumns,
    final List titles) {
    this(cssClass, numColumns, titles, Collections.EMPTY_LIST);
  }

  public TableLayout(final String cssClass, final int numColumns,
    final List titles, final List cssClasses) {
    this.cssClass = cssClass;
    this.numColumns = numColumns;
    this.titles = titles;
    this.cssClasses.addAll(cssClasses);
    for (int i = cssClasses.size(); i < numColumns; i++) {
      this.cssClasses.add("");
    }
  }

  public void serialize(final XmlWriter out, final ElementContainer container)
    throws IOException {
    if (!container.getElements().isEmpty()) {
      out.startTag(HtmlUtil.DIV);
      if (cssClass != null) {
        out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
      }
      out.startTag(HtmlUtil.TABLE);
      out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
      out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");

      serializeThead(out);

      serializeTbody(out, container);
      out.endTag(HtmlUtil.TABLE);
      out.endTag(HtmlUtil.DIV);
    }
  }

  private void serializeTbody(final XmlWriter out,
    final ElementContainer container) throws IOException {
    out.startTag(HtmlUtil.TBODY);
    List elementList = container.getElements();
    int i = 0;
    int rowNum = 0;
    int numElements = elementList.size();
    int lastRow = (numElements - 1) / numColumns;
    for (Iterator elements = elementList.iterator(); elements.hasNext();) {
      Element element = (Element)elements.next();
      int col = i % numColumns;
      String colCss = (String)cssClasses.get(col);
      boolean firstCol = col == 0;
      boolean lastCol = (i + 1) % numColumns == 0 || i == numElements - 1;
      if (firstCol) {
        out.startTag(HtmlUtil.TR);
        String rowCss = "";
        if (rowNum == 0) {
          rowCss += " firstRow";
        }
        if (rowNum == lastRow) {
          rowCss += " lastRow";
        }
        if (rowCss.length() > 0) {
          out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
        }
        colCss += " firstCol";
      }
      if (lastCol) {
        colCss += " lastCol";
      }
      out.startTag(HtmlUtil.TD);
      if (colCss.length() > 0) {
        out.attribute(HtmlUtil.ATTR_CLASS, colCss);
      }
      element.serialize(out);
      out.endTag(HtmlUtil.TD);
      i++;
      if (lastCol) {
        out.endTag(HtmlUtil.TR);
        rowNum++;
      }
    }
    out.endTag(HtmlUtil.TBODY);
  }

  private void serializeThead(final XmlWriter out) throws IOException {
    if (titles != null && !titles.isEmpty()) {
      out.startTag(HtmlUtil.THEAD);
      out.startTag(HtmlUtil.TR);
      int col = 0;
      for (Iterator titleIter = titles.iterator(); titleIter.hasNext();) {
        String title = (String)titleIter.next();
        out.startTag(HtmlUtil.TH);
        String colCssClass = (String)cssClasses.get(col);
        if (col == 0) {
          colCssClass += " firstCol";
        }
        if (col == numColumns) {
          colCssClass += " lastCol";
        }
        if (colCssClass.length() > 0) {
          out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
        }
        out.text(title);
        out.endTag(HtmlUtil.TH);
        col++;
      }
      out.endTag(HtmlUtil.TR);
      out.endTag(HtmlUtil.THEAD);
    }
  }
}
