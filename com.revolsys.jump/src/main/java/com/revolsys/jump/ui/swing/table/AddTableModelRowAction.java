package com.revolsys.jump.ui.swing.table;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import com.revolsys.jump.ui.swing.EditPanel;
import com.revolsys.jump.ui.swing.EditPanelDialog;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public final class AddTableModelRowAction<T> implements ActionListener {

  private JComponent owner;

  private JTable table;

  private ObjectTableModel<T> tableModel;

  private EditPanel<T> panel;

  private WorkbenchContext workbenchContext;

  public AddTableModelRowAction(final WorkbenchContext workbenchContext,
    final JComponent owner, final JTable table,
    final ObjectTableModel<T> tableModel, final EditPanel<T> panel) {
    this.workbenchContext = workbenchContext;
    this.owner = owner;
    this.table = table;
    this.tableModel = tableModel;
    this.panel = panel;
  }

  public void actionPerformed(final ActionEvent e) {
    panel.reset();

    EditPanelDialog<T> dialog;
    Window window = SwingUtilities.windowForComponent(owner);
    if (window instanceof Frame) {
      dialog = new EditPanelDialog<T>(workbenchContext, (Frame)window, panel);
    } else if (window instanceof JDialog) {
      dialog = new EditPanelDialog<T>(workbenchContext, (JDialog)window, panel);
    } else {
      return;
    }
    dialog.setVisible(true);
    if (dialog.wasOKPressed()) {
      T row = dialog.getValue();
      int selectedRow = table.getSelectedRow();
      if (selectedRow == -1) {
        tableModel.addRow(row);
      } else {
        tableModel.insertRow(selectedRow, row);
      }
    }
  }
}
