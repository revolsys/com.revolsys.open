package com.revolsys.swing.table.lambda.column;

import javax.swing.table.TableColumn;

import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJTable;

public interface TableModelColumn {

  void applySettings(BaseJTable table, final TableColumn tableColumn);

  Class<?> getColumnClass();

  String getColumnName();

  default BaseJPopupMenu getHeaderMenu() {
    final MenuFactory headerMenuFactory = getHeaderMenuFactory();
    if (headerMenuFactory == null) {
      return null;
    } else {
      return headerMenuFactory.newJPopupMenu();
    }
  }

  MenuFactory getHeaderMenuFactory();

  Object getValueAt(int rowIndex);

  boolean isCellEditable(int rowIndex);

  void setValueAt(Object value, int rowIndex);
}
