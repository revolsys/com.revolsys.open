package com.revolsys.swing.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.swing.parallel.Invoke;

public class JTableUtil {

  public static void sizeColumnsToFit(final JTable table) {
    Invoke.later(() -> {
      final JTableHeader tableHeader = table.getTableHeader();
      final TableColumnModel columnModel = table.getColumnModel();
      for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
        final TableColumn column = columnModel.getColumn(columnIndex);
        final String columnName = table.getColumnName(columnIndex);
        final TableCellRenderer headerRenderer = tableHeader.getDefaultRenderer();
        final int headerWidth = headerRenderer
          .getTableCellRendererComponent(table, columnName, false, false, 0, 0)
          .getPreferredSize().width;
        int maxwidth = headerWidth + 20;
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
          final TableCellRenderer cellRenderer = table.getCellRenderer(rowIndex, columnIndex);
          final Object value = table.getValueAt(rowIndex, columnIndex);
          final Component component = cellRenderer.getTableCellRendererComponent(table, value,
            false, false, rowIndex, columnIndex);
          maxwidth = Math.max(component.getPreferredSize().width, maxwidth);
        }
        column.setPreferredWidth(maxwidth + 5);
      }
    });
  }
}
