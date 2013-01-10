package com.revolsys.swing.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class JTableUtil {

  public static void sizeColumnsToFit(JTable table) {
    JTableHeader tableHeader = table.getTableHeader();
    TableColumnModel columnModel = table.getColumnModel();
    for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
      TableColumn column = columnModel.getColumn(columnIndex);
      String columnName = table.getColumnName(columnIndex);
      TableCellRenderer headerRenderer = tableHeader.getDefaultRenderer();
      int headerWidth = headerRenderer.getTableCellRendererComponent(table,
        columnName, false, false, 0, 0).getPreferredSize().width;
      int maxwidth = headerWidth + 20;
      for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
        TableCellRenderer cellRenderer = table.getCellRenderer(rowIndex,
          columnIndex);
        Object value = table.getValueAt(rowIndex, columnIndex);
        Component component = cellRenderer.getTableCellRendererComponent(table,
          value, false, false, rowIndex, columnIndex);
        maxwidth = Math.max(component.getPreferredSize().width, maxwidth);
      }
      column.setPreferredWidth(maxwidth + 5);
    }
  }
}
