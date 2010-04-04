package com.revolsys.jump.ui.swing.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

public class DeleteTableModelRowAction implements ActionListener {
  private JTable table;

  private ObjectTableModel<?> model;

  public DeleteTableModelRowAction(final JTable table,
    final ObjectTableModel<?> model) {
    this.table = table;
    this.model = model;
  }

  public void actionPerformed(final ActionEvent e) {
    int[] selectedRows = table.getSelectedRows();
    for (int i = 0; i < selectedRows.length; i++) {
      int rowIndex = selectedRows[i];
      model.removeRow(rowIndex);
    }
  }

}
