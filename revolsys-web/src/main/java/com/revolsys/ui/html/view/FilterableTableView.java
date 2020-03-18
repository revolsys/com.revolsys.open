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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.TextAreaField;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.layout.TableBodyLayout;
import com.revolsys.ui.html.serializer.KeySerializerTableSerializer;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class FilterableTableView extends ElementContainer {
  private String cssClass = "table";

  private final KeySerializerTableSerializer model;

  private String noRecordsMessgae = "No records found";

  private Map<String, Element> searchFields;

  public FilterableTableView(final KeySerializerTableSerializer model,
    final Map<String, Element> searchFields, final String cssClass) {
    this.model = model;
    this.searchFields = searchFields;
    this.cssClass = cssClass;
  }

  public FilterableTableView(final KeySerializerTableSerializer model, final String cssClass) {
    this.model = model;
    this.cssClass = cssClass;
  }

  /**
   * @return Returns the noRecordsMessgae.
   */
  public String getNoRecordsMessgae() {
    return this.noRecordsMessgae;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    if (this.searchFields != null) {
      final ElementContainer searchContainer = new ElementContainer(
        new TableBodyLayout("search", this.model.getColumnCount()));
      add(searchContainer);
      for (final KeySerializer serializer : this.model.getSerializers()) {
        final String name = serializer.getName();
        Element element = this.searchFields.get(name);
        if (element == null) {
          element = NbspElement.INSTANCE;
        } else {
          element = element.clone();
          if (element instanceof Field) {
            final Field field = (Field)element;
            field.setRequired(false);
          }
          if (element instanceof TextField) {
            final TextField textField = (TextField)element;
            textField.setSize(1);
          }
          if (element instanceof TextAreaField) {
            final TextAreaField textField = (TextAreaField)element;
            textField.setRows(1);
            textField.setCols(1);
          }
        }
        searchContainer.add(element);
      }
    }
    super.initialize(request);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final int rowCount = this.model.getBodyRowCount();
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, this.cssClass);

    out.startTag(HtmlElem.TABLE);
    out.attribute(HtmlAttr.CELL_SPACING, "0");
    out.attribute(HtmlAttr.CELL_PADDING, "0");
    out.attribute(HtmlAttr.CLASS, "data");

    serializeHeadings(out);
    serializeFooter(out);
    serializeRows(out);

    out.endTag(HtmlElem.TABLE);
    if (rowCount == 0) {
      out.startTag(HtmlElem.I);
      out.text(this.noRecordsMessgae);
      out.endTag(HtmlElem.I);
    }
    out.endTag(HtmlElem.DIV);
  }

  protected void serializeFooter(final XmlWriter out) {
    final int rowCount = this.model.getFooterRowCount();
    if (rowCount > 0) {
      out.startTag(HtmlElem.TFOOT);
      for (int row = 0; row < rowCount; row++) {
        serializeFooterRow(out, row, rowCount);
      }
      out.endTag(HtmlElem.TFOOT);
    }
  }

  protected void serializeFooterRow(final XmlWriter out, final int row, final int rowCount) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlElem.TR);
    String rowCss = "";
    if (row == 0) {
      rowCss += " firstRow";
    }
    if (row == rowCount - 1) {
      rowCss += " lastRow";
    }
    if (rowCss.length() > 0) {
      out.attribute(HtmlAttr.CLASS, rowCss);
    }
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlElem.TD);
      String colCssClass = this.model.getFooterCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlAttr.CLASS, colCssClass);
      this.model.serializeFooterCell(out, row, col);
      out.endTag(HtmlElem.TD);
    }
    out.endTag(HtmlElem.TR);
  }

  protected void serializeHeadings(final XmlWriter out) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlElem.THEAD);
    out.startTag(HtmlElem.TR);
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlElem.TH);
      String colCssClass = this.model.getHeaderCssClass(col);
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
        out.attribute(HtmlAttr.CLASS, colCssClass);
      }
      this.model.serializeHeaderCell(out, col);
      out.endTag(HtmlElem.TH);
    }
    out.endTag(HtmlElem.TR);
    out.endTag(HtmlElem.THEAD);
  }

  protected void serializeRow(final XmlWriter out, final int row, final int rowCount) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlElem.TR);
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
      out.attribute(HtmlAttr.CLASS, rowCss);
    }
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlElem.TD);
      String colCssClass = this.model.getBodyCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlAttr.CLASS, colCssClass);
      this.model.serializeBodyCell(out, row, col);
      out.endTag(HtmlElem.TD);
    }
    out.endTag(HtmlElem.TR);
  }

  protected void serializeRows(final XmlWriter out) {
    for (final Element element : getElements()) {
      element.serialize(out);
    }

    out.startTag(HtmlElem.TBODY);
    final int rowCount = this.model.getBodyRowCount();
    for (int row = 0; row < rowCount; row++) {
      serializeRow(out, row, rowCount);
    }
    out.endTag(HtmlElem.TBODY);
  }

  /**
   * @param noRecordsMessgae The noRecordsMessgae to set.
   */
  public void setNoRecordsMessgae(final String noRecordsMessgae) {
    this.noRecordsMessgae = noRecordsMessgae;
  }

  public void setRows(final List<?> results) {
    this.model.setRows(results);
  }
}
