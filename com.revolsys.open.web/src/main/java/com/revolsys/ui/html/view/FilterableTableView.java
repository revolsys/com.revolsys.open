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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.layout.TableBodyLayout;
import com.revolsys.ui.html.serializer.RowsTableSerializer;

public class FilterableTableView extends ElementContainer {
  private String cssClass = "table";

  private RowsTableSerializer model;

  private String title;

  private String noRecordsMessgae = "No records found";

  private HtmlUiBuilder htmlUiBuilder;

  private List<String> keyList;

  public FilterableTableView(final HtmlUiBuilder htmlUiBuilder,
    final RowsTableSerializer model, final String cssClass, final String title,
    final List<String> keyList) {
    this.model = model;
    this.cssClass = cssClass;
    this.title = title;
    this.htmlUiBuilder = htmlUiBuilder;
    this.keyList = keyList;
  }

  public void initialize(HttpServletRequest request) {
    ElementContainer searchFields = new ElementContainer(new TableBodyLayout(
      "search", keyList.size()));
    add(searchFields);
    for (String key : keyList) {
      Element element = htmlUiBuilder.getField(request, key).clone();
      if (element instanceof Field) {
        Field field = (Field)element;
        field.setRequired(false);
      }
      if (element instanceof TextField) {
        TextField textField = (TextField)element;
        textField.setSize(1);
      }
      searchFields.add(element);
    }
    super.initialize(request);
  }

  public void serializeElement(final XmlWriter out) {
    int rowCount = model.getBodyRowCount();
    int colCount = model.getColumnCount();
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    if (title != null && title.length() > 0) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "title");
      out.text(title);
      out.endTag(HtmlUtil.DIV);
    }

    out.startTag(HtmlUtil.TABLE);
    out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
    out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");
    out.attribute(HtmlUtil.ATTR_CLASS, "data");

    serializeHeadings(out);
    serializeFooter(out);
    serializeRows(out);

    out.endTag(HtmlUtil.TABLE);
    if (rowCount == 0) {
      out.startTag(HtmlUtil.I);
      out.text(noRecordsMessgae);
      out.endTag(HtmlUtil.I);
    }
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeHeadings(final XmlWriter out) {
    int colCount = model.getColumnCount();
    out.startTag(HtmlUtil.THEAD);
    out.startTag(HtmlUtil.TR);
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TH);
      String colCssClass = model.getHeaderCssClass(col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      }
      if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      if (colCssClass.length() > 0) {
        out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      }
      model.serializeHeaderCell(out, col);
      out.endTag(HtmlUtil.TH);
    }
    out.endTag(HtmlUtil.TR);
    out.endTag(HtmlUtil.THEAD);
  }

  protected void serializeRows(final XmlWriter out) {
    for (Element element : getElements()) {
      element.serialize(out);
    }

    out.startTag(HtmlUtil.TBODY);
    int rowCount = model.getBodyRowCount();
    for (int row = 0; row < rowCount; row++) {
      serializeRow(out, row, rowCount);
    }
    out.endTag(HtmlUtil.TBODY);
  }

  protected void serializeRow(
    final XmlWriter out,
    final int row,
    final int rowCount) {
    int colCount = model.getColumnCount();
    out.startTag(HtmlUtil.TR);
    String rowCss = "";
    if (row == 0) {
      rowCss += " firstRow";
    }
    if (row == rowCount - 1) {
      rowCss += " lastRow";
    }
    if (row % 2 == 1) {
      rowCss += " even";
    }
    if (rowCss.length() > 0) {
      out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
    }
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TD);
      String colCssClass = model.getBodyCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      model.serializeBodyCell(out, row, col);
      out.endTag(HtmlUtil.TD);
    }
    out.endTag(HtmlUtil.TR);
  }

  protected void serializeFooter(final XmlWriter out) {
    int rowCount = model.getFooterRowCount();
    if (rowCount > 0) {
      out.startTag(HtmlUtil.TFOOT);
      for (int row = 0; row < rowCount; row++) {
        serializeFooterRow(out, row, rowCount);
      }
      out.endTag(HtmlUtil.TFOOT);
    }
  }

  protected void serializeFooterRow(
    final XmlWriter out,
    final int row,
    final int rowCount) {
    int colCount = model.getColumnCount();
    out.startTag(HtmlUtil.TR);
    String rowCss = "";
    if (row == 0) {
      rowCss += " firstRow";
    }
    if (row == rowCount - 1) {
      rowCss += " lastRow";
    }
    if (rowCss.length() > 0) {
      out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
    }
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TD);
      String colCssClass = model.getFooterCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      model.serializeFooterCell(out, row, col);
      out.endTag(HtmlUtil.TD);
    }
    out.endTag(HtmlUtil.TR);
  }

  /**
   * @return Returns the noRecordsMessgae.
   */
  public String getNoRecordsMessgae() {
    return noRecordsMessgae;
  }

  /**
   * @param noRecordsMessgae The noRecordsMessgae to set.
   */
  public void setNoRecordsMessgae(String noRecordsMessgae) {
    this.noRecordsMessgae = noRecordsMessgae;
  }

  public void setRows(List<?> results) {
    model.setRows(results);
  }
}
