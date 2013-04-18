package com.revolsys.swing.table;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

public class BaseJxTable extends JXTable {
  public BaseJxTable() {
    setAutoCreateRowSorter(false);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));

    final TableCellRenderer headerRenderer = new SortableTableCellHeaderRenderer();
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setDefaultRenderer(headerRenderer);
    tableHeader.setReorderingAllowed(false);
  }

  public BaseJxTable(TableModel model) {
    this();
    setModel(model);

  }

  public void setModel(TableModel model) {
    boolean createColumns = getAutoCreateColumnsFromModel();
    if (createColumns) {
      setAutoCreateColumnsFromModel(false);
    }
    try {
      super.setModel(model);
    } finally {
      if (createColumns) {
        setAutoCreateColumnsFromModel(true);
      }
      if (isSortable()) {
        setRowSorter(createDefaultRowSorter());
      }
    }
  }

  @Override
  public void setSortable(boolean sortable) {
    super.setSortable(sortable);
    if (sortable) {
      if (getRowSorter() == null) {
        setRowSorter(createDefaultRowSorter());
      }
    } else {
      setRowSorter(null);
    }
  }

  @Override
  public RowSorter<? extends TableModel> getRowSorter() {
    if (isSortable()) {
      return super.getRowSorter();
    } else {
      return null;
    }
  }

  protected void createDefaultRenderers() {
    super.createDefaultRenderers();
    setDefaultRenderer(Object.class, new DefaultTableRenderer(
      new StringConverterValue()));

  }
}
