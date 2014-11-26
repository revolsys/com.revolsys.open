package com.revolsys.ui.html.view;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.serializer.TableSerializer;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class TableView extends Element {
  private String cssClass = "table";

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
      this.cssClass += " " + cssClass;
    }
  }

  public TableView(final TableSerializer model, final String cssClass,
    final String title, final String noRecordsMessgae) {
    this.model = model;
    if (cssClass != null && cssClass.trim().length() > 0) {
      this.cssClass += " " + cssClass;
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
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_ID, this.id);
    out.attribute(HtmlUtil.ATTR_CLASS, this.cssClass);
    if (this.title != null && this.title.length() > 0) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "title");
      out.text(this.title);
      out.endTag(HtmlUtil.DIV);
    }
    if (rowCount > 0 || !Property.hasValue(this.noRecordsMessgae)) {
      out.startTag(HtmlUtil.TABLE);
      out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
      out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");
      out.attribute(HtmlUtil.ATTR_CLASS, "data display cell-border");
      out.attribute(HtmlUtil.ATTR_WIDTH, this.width);

      serializeHeadings(out);
      serializeFooter(out);
      serializeRows(out);

      out.endTag(HtmlUtil.TABLE);
    } else {
      out.startTag(HtmlUtil.I);
      out.text(this.noRecordsMessgae);
      out.endTag(HtmlUtil.I);
    }
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeFooter(final XmlWriter out) {
    final int rowCount = this.model.getFooterRowCount();
    if (rowCount > 0) {
      out.startTag(HtmlUtil.TFOOT);
      for (int row = 0; row < rowCount; row++) {
        serializeFooterRow(out, row, rowCount);
      }
      out.endTag(HtmlUtil.TFOOT);
    }
  }

  protected void serializeFooterRow(final XmlWriter out, final int row,
    final int rowCount) {
    final int colCount = this.model.getColumnCount();
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
      String colCssClass = this.model.getFooterCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      this.model.serializeFooterCell(out, row, col);
      out.endTag(HtmlUtil.TD);
    }
    out.endTag(HtmlUtil.TR);
  }

  protected void serializeHeadings(final XmlWriter out) {
    final int colCount = this.model.getColumnCount();
    out.startTag(HtmlUtil.THEAD);
    out.startTag(HtmlUtil.TR);
    for (int col = 0; col < colCount; col++) {
      out.startTag(HtmlUtil.TH);
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
        out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      }
      this.model.serializeHeaderCell(out, col);
      out.endTag(HtmlUtil.TH);
    }
    out.endTag(HtmlUtil.TR);
    out.endTag(HtmlUtil.THEAD);
  }

  protected void serializeRow(final XmlWriter out, final int row,
    final int rowCount) {
    final int colCount = this.model.getColumnCount();
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
      String colCssClass = this.model.getBodyCssClass(row, col);
      if (colCssClass == null) {
        colCssClass = "";
      }
      if (col == 0) {
        colCssClass += " firstCol";
      } else if (col == colCount - 1) {
        colCssClass += " lastCol";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, colCssClass);
      this.model.serializeBodyCell(out, row, col);
      out.endTag(HtmlUtil.TD);
    }
    out.endTag(HtmlUtil.TR);
  }

  protected void serializeRows(final XmlWriter out) {
    out.startTag(HtmlUtil.TBODY);
    final int rowCount = this.model.getBodyRowCount();
    for (int row = 0; row < rowCount; row++) {
      serializeRow(out, row, rowCount);
    }
    out.endTag(HtmlUtil.TBODY);
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
