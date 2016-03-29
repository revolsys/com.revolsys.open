package com.revolsys.ui.html.view;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.serializer.TableSerializer;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class TableView extends Element {
  private String cssClass = null;

  private String id;

  private final TableSerializer model;

  private String noRecordsMessgae = "No records found";

  private String title;

  private String width;

  public TableView(final TableSerializer model) {
    this.model = model;
  }

  public TableView(final TableSerializer model, final String cssClass) {
    this.model = model;
    if (cssClass != null && cssClass.trim().length() > 0) {
      this.cssClass = Strings.toString(" ", this.cssClass, cssClass);
    }
  }

  public TableView(final TableSerializer model, final String cssClass, final String title,
    final String noRecordsMessgae) {
    this.model = model;
    if (cssClass != null && cssClass.trim().length() > 0) {
      this.cssClass = Strings.toString(" ", this.cssClass, cssClass);
    }
    if (title != null) {
      this.title = title.trim();
    }
    this.title = title;
    if (Property.hasValue(noRecordsMessgae)) {
      this.noRecordsMessgae = noRecordsMessgae;
    }
  }

  public String getId() {
    return this.id;
  }

  /**
   * @return Returns the noRecordsMessgae.
   */
  public String getNoRecordsMessgae() {
    return this.noRecordsMessgae;
  }

  public String getWidth() {
    return this.width;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final int rowCount = this.model.getBodyRowCount();
    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.ID, this.id);
    if (Property.hasValue(this.cssClass)) {
      out.attribute(HtmlAttr.CLASS, this.cssClass + " table-responsive");
    } else {
      out.attribute(HtmlAttr.CLASS, "table-responsive");
    }
    if (this.title != null && this.title.length() > 0) {
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, "title");
      out.text(this.title);
      out.endTag(HtmlElem.DIV);
    }
    if (rowCount > 0 || !Property.hasValue(this.noRecordsMessgae)) {
      out.startTag(HtmlElem.TABLE);
      out.attribute(HtmlAttr.ROLE, "table");
      out.attribute(HtmlAttr.CELL_SPACING, "0");
      out.attribute(HtmlAttr.CELL_PADDING, "0");
      out.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");
      if (Property.hasValue(this.width)) {
        out.attribute(HtmlAttr.STYLE, "width:" + this.width);
      }

      serializeHeadings(out);
      serializeFooter(out);
      serializeRows(out);

      out.endTag(HtmlElem.TABLE);
    } else {
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
    out.startTag(HtmlElem.TBODY);
    final int rowCount = this.model.getBodyRowCount();
    for (int row = 0; row < rowCount; row++) {
      serializeRow(out, row, rowCount);
    }
    out.endTag(HtmlElem.TBODY);
  }

  public void setId(final String id) {
    this.id = id;
  }

  /**
   * @param noRecordsMessgae The noRecordsMessgae to set.
   */
  public void setNoRecordsMessgae(final String noRecordsMessgae) {
    this.noRecordsMessgae = noRecordsMessgae;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setWidth(final String width) {
    this.width = width;
  }

}
