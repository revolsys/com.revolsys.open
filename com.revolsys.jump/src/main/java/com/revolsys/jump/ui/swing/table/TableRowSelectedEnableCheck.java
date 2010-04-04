package com.revolsys.jump.ui.swing.table;

import javax.swing.JComponent;
import javax.swing.JTable;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;

public class TableRowSelectedEnableCheck implements EnableCheck {
  private JTable table;

  public TableRowSelectedEnableCheck(final JTable table) {
    this.table = table;
  }

  public String check(final JComponent jcomponent) {
    if (table.getSelectedRow() != -1) {
      return null;
    } else {
      return "No rows selected";
    }
  }
}
