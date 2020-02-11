package com.revolsys.swing.table;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class BaseColumnFactory extends ColumnFactory {
  private final int sortWidth = ((Icon)UIManager.get("Table.ascendingSortIcon")).getIconWidth();

  @Override
  protected int calcHeaderWidth(final JXTable table, final TableColumnExt tableColumn) {
    int width = super.calcHeaderWidth(table, tableColumn);
    width += this.sortWidth;
    final TableCellRenderer renderer = getHeaderRenderer(table, tableColumn);
    if (renderer instanceof DefaultTableCellRenderer) {
      final DefaultTableCellRenderer defaultTableCellRenderer = (DefaultTableCellRenderer)renderer;
      final int position = defaultTableCellRenderer.getHorizontalTextPosition();
      if (position > 0) {
        width += position;
      }
    }
    return width;
  }

  @Override
  public void configureColumnWidths(final JXTable table, final TableColumnExt tableColumn) {
    super.configureColumnWidths(table, tableColumn);
    final int preferredWidth = tableColumn.getPreferredWidth();
    tableColumn.setWidth(preferredWidth);
  }

  @Override
  public void configureTableColumn(final TableModel model, final TableColumnExt tableColumn) {
    final AbstractTableModel tableModel = (AbstractTableModel)model;
    super.configureTableColumn(model, tableColumn);
    int columnIndex = tableColumn.getModelIndex();
    final BaseJTable table = tableModel.getTable();
    columnIndex = table.convertColumnIndexToModel(columnIndex);
    tableColumn.setSortable(tableModel.isColumnSortable(columnIndex));
    final Object prototypeValue = tableModel.getPrototypeValue(columnIndex);
    tableColumn.setPrototypeValue(prototypeValue);
  }

}
