package com.revolsys.swing.table.lambda.column;

import javax.swing.table.TableColumn;

import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJTable;

public interface TableModelColumn {

  void applySettings(BaseJTable table, final TableColumn tableColumn);

  Class<?> getColumnClass();

  String getColumnName();

  MenuFactory getHeaderMenuFactory();

  BaseJPopupMenu getMenu();

  BaseJPopupMenu getMenu(int rowIndex);

  Object getValueAt(int rowIndex);

  boolean isCellEditable(int rowIndex);

  void setValueAt(Object value, int rowIndex);
}
